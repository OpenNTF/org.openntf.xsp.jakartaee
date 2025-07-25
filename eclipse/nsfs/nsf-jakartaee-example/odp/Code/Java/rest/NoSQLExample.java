/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jnosql.communication.driver.attachment.EntityAttachment;
import org.openntf.xsp.jakarta.nosql.communication.driver.ByteArrayEntityAttachment;
import org.openntf.xsp.jakarta.nosql.communication.driver.ViewInfo;
import org.openntf.xsp.jakarta.nosql.mapping.extension.FTSearchOption;
import org.openntf.xsp.jakarta.nosql.mapping.extension.ViewQuery;

import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.StreamUtil;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.ContentDisposition;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimePart;
import jakarta.mvc.Controller;
import jakarta.mvc.Models;
import jakarta.data.page.PageRequest;
import jakarta.data.Sort;
import jakarta.transaction.UserTransaction;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import model.CustomPropertyType;
import model.Person;
import model.PersonRepository;
import model.ServerRepository;

@Path("nosql")
public class NoSQLExample {
	@Inject
	PersonRepository personRepository;
	
	@Inject
	ServerRepository serverRepository;

	@Inject
	Models models;
	
	@Inject
	UserTransaction transaction;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Object getByLastName(@QueryParam("lastName") String lastName) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("byQueryLastName", personRepository.findByLastName(lastName).collect(Collectors.toList()));
		result.put("totalCount", personRepository.countBy());
		return result;
	}
	
	@Path("inFolder")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Person> getInFolder() {
		return personRepository.findInPersonsFolder().collect(Collectors.toList());
	}
	
	@Path("inFolderManual")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Person> getInFolderManual() {
		return personRepository.readViewEntries(PersonRepository.FOLDER_PERSONS, -1, false, null, null, null)
			.collect(Collectors.toList());
	}
	
	@Path("servers")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Object getServers() {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("all", serverRepository.findAll(Sort.asc("serverName")).collect(Collectors.toList()));
		result.put("totalCount", serverRepository.countBy());
		return result;
	}
	
	@Path("create")
	@GET
	@Produces(MediaType.TEXT_HTML)
	@Controller
	public String getCreationPage() {
		return "person-create.jsp";
	}
	
	@Path("create")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Controller
	public String createPerson(
			@FormParam("firstName") @NotEmpty String firstName,
			@FormParam("lastName") String lastName,
			@FormParam("birthday") String birthday,
			@FormParam("favoriteTime") String favoriteTime,
			@FormParam("added") String added,
			@FormParam("customProperty") String customProperty,
			@FormParam("email") String email
	) throws Exception {
		transaction.begin();
		try {
			Person person = new Person();
			composePerson(person, firstName, lastName, birthday, favoriteTime, added, customProperty, null);
			person.setEmail(email);
			
			personRepository.save(person);
			transaction.commit();
			return "redirect:nosql/list";
		} catch(Exception e) {
			transaction.rollback();
			throw e;
		}
	}
	
	@Path("create")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_HTML)
	@Controller
	public String createPerson(MimeMultipart body) throws Exception {
		String firstName = "";
		String lastName = "";
		String birthday = "";
		String favoriteTime = "";
		String added = "";
		String customProperty = "";
		boolean intentionallyRollBack = false;
		
		List<EntityAttachment> attachments = new ArrayList<>();
		for(int i = 0; i < body.getCount(); i++) {
			MimePart part = (MimePart)body.getBodyPart(i);
			String dispositionValue = part.getHeader(HttpHeaders.CONTENT_DISPOSITION, null);
			if(StringUtil.isNotEmpty(dispositionValue)) {
				ContentDisposition disposition = new ContentDisposition(dispositionValue);
				String name = disposition.getParameter("name");
				switch(StringUtil.toString(name)) {
				case "firstName":
					firstName = StreamUtil.readString(part.getInputStream());
					break;
				case "lastName":
					lastName = StreamUtil.readString(part.getInputStream());
					break;
				case "birthday":
					birthday = StreamUtil.readString(part.getInputStream());
					break;
				case "favoriteTime":
					favoriteTime = StreamUtil.readString(part.getInputStream());
					break;
				case "added":
					added = StreamUtil.readString(part.getInputStream());
					break;
				case "customProperty":
					customProperty = StreamUtil.readString(part.getInputStream());
					break;
				case "intentionallyRollBack":
					intentionallyRollBack = Boolean.parseBoolean(StreamUtil.readString(part.getInputStream()));
					break;
				case "attachment":
					String fileName = disposition.getParameter("filename");
					if(StringUtil.isEmpty(fileName)) {
						// Then assume there's no actual attachment
						continue;
					}
					String contentType = part.getHeader(HttpHeaders.CONTENT_TYPE, null);
					if(StringUtil.isEmpty(contentType)) {
						contentType = MediaType.APPLICATION_OCTET_STREAM;
					}
					
					// Ideally, this should go to a temp file
					byte[] data;
					try(
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						InputStream is = part.getInputStream();
					) {
						StreamUtil.copyStream(is, baos);
						data = baos.toByteArray();
					}
					
					EntityAttachment att = new ByteArrayEntityAttachment(fileName, contentType, Instant.now().toEpochMilli(), data);
					attachments.add(att);
					
					break;
				default:
					break;
				}
				
			}
		}
		
		transaction.begin();
		try {
			Person person = new Person();
			composePerson(person, firstName, lastName, birthday, favoriteTime, added, customProperty, null);
			person.setAttachments(attachments);

			personRepository.save(person);
			if(intentionallyRollBack) {
				throw new RuntimeException("I was asked to intentionally roll back");
			}
			//transaction.commit();
			return "redirect:nosql/list";
		} catch(Exception e) {
			transaction.rollback();
			throw e;
		}
	}
	
	@Path("create")
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	public Person createPersonJson(MimeMultipart body) throws MessagingException, IOException {
		String firstName = "";
		String lastName = "";
		String birthday = "";
		String favoriteTime = "";
		String added = "";
		String customProperty = "";
		String email = "";
		
		List<EntityAttachment> attachments = new ArrayList<>();
		for(int i = 0; i < body.getCount(); i++) {
			MimePart part = (MimePart)body.getBodyPart(i);
			String dispositionValue = part.getHeader(HttpHeaders.CONTENT_DISPOSITION, null);
			if(StringUtil.isNotEmpty(dispositionValue)) {
				ContentDisposition disposition = new ContentDisposition(dispositionValue);
				String name = disposition.getParameter("name");
				switch(StringUtil.toString(name)) {
				case "firstName":
					firstName = StreamUtil.readString(part.getInputStream());
					break;
				case "lastName":
					lastName = StreamUtil.readString(part.getInputStream());
					break;
				case "birthday":
					birthday = StreamUtil.readString(part.getInputStream());
					break;
				case "favoriteTime":
					favoriteTime = StreamUtil.readString(part.getInputStream());
					break;
				case "added":
					added = StreamUtil.readString(part.getInputStream());
					break;
				case "customProperty":
					customProperty = StreamUtil.readString(part.getInputStream());
					break;
				case "email":
					email = StreamUtil.readString(part.getInputStream());
					break;
				case "attachment":
					String fileName = disposition.getParameter("filename");
					if(StringUtil.isEmpty(fileName)) {
						throw new IllegalArgumentException("attachment part must have a file name");
					}
					String contentType = part.getHeader(HttpHeaders.CONTENT_TYPE, null);
					if(StringUtil.isEmpty(contentType)) {
						contentType = MediaType.APPLICATION_OCTET_STREAM;
					}
					
					// Ideally, this should go to a temp file
					byte[] data;
					try(
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						InputStream is = part.getInputStream();
					) {
						StreamUtil.copyStream(is, baos);
						data = baos.toByteArray();
					}

					EntityAttachment att = new ByteArrayEntityAttachment(fileName, contentType, Instant.now().toEpochMilli(), data);
					attachments.add(att);
					
					break;
				default:
					break;
				}
				
			}
		}
		
		Person person = new Person();
		composePerson(person, firstName, lastName, birthday, favoriteTime, added, customProperty, email);
		person.setAttachments(attachments);

		return personRepository.save(person);
	}
			
	
	@Path("list")
	@GET
	@Produces(MediaType.TEXT_HTML)
	@Controller
	public String list(@QueryParam("sortCol") String sortCol) {
		if(sortCol == null || sortCol.isEmpty()) {
			models.put("persons", personRepository.findAll().collect(Collectors.toList()));
		} else {
			models.put("persons", personRepository.findAll(Sort.asc(sortCol)).collect(Collectors.toList()));
		}
		return "person-list.jsp";
	}
	
	@Path("list")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Person> listJson(@QueryParam("sortCol") String sortCol) {
		if(sortCol == null || sortCol.isEmpty()) {
			return personRepository.findAll().collect(Collectors.toList());
		} else {
			return personRepository.findAll(Sort.asc(sortCol)).collect(Collectors.toList());
		}
	}
	
	@Path("ftSearch")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Person> ftSearchView(@QueryParam("search") String search, @QueryParam("search2") String search2) {
		if(search2 == null || search2.isEmpty()) {
			return personRepository.findByKeyMulti(
				ViewQuery.query().ftSearch(search, EnumSet.of(FTSearchOption.UPDATE_INDEX)),
				null,
				null
			).collect(Collectors.toList());
		} else {
			return personRepository.findByKeyMulti(
				ViewQuery.query().ftSearch(Arrays.asList(search, search2), EnumSet.of(FTSearchOption.UPDATE_INDEX)),
				null,
				null
			).collect(Collectors.toList());
		}
	}
	
	@Path("ftSearchSorted")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Person> ftSearchViewSorted(@QueryParam("search") String search) {
		return personRepository.findByKeyMulti(
			ViewQuery.query().ftSearch(search, EnumSet.of(FTSearchOption.UPDATE_INDEX)),
			Sort.desc("firstName"),
			null
		).collect(Collectors.toList());
	}
	
	@Path("ftSearchPaginated")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Person> ftSearchViewPaginated(@QueryParam("search") String search, @QueryParam("page") int page, @QueryParam("size") int size) {
		return personRepository.findByKeyMulti(
			ViewQuery.query().ftSearch(search, EnumSet.of(FTSearchOption.UPDATE_INDEX)),
			null,
			PageRequest.ofPage(page).size(size)
		).collect(Collectors.toList());
	}
	
	@Path("{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Person getPerson(@PathParam("id") String id) {
		return personRepository.findById(id)
			.orElseThrow(() -> new NotFoundException("Unable to find Person for ID " + id));
	}
	
	@Path("byEmail/{email}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Person getPersonByEmail(@PathParam("email") String email) {
		return personRepository.findByEmail(email)
			.orElseThrow(() -> new NotFoundException("Unable to find Person for email address " + email));
	}
	
	@Path("{id}")
	@GET
	@Controller
	@Produces(MediaType.TEXT_HTML)
	public String show(@PathParam("id") String id) {
		models.put("person", personRepository.findById(id).get());
		return "person-show.jsp";
	}
	
	@Path("{id}/attachment/{attachmentName}")
	@GET
	public Response getAttachment(@PathParam("id") String id, @PathParam("attachmentName") String attachmentName) {
		Person person = personRepository.findById(id).get();
		
		String name = attachmentName.replace('+', ' ');
		EntityAttachment att = person.getAttachments()
			.stream()
			.filter(a -> a.getName().equals(name))
			.findFirst()
			.orElseThrow(() -> new NotFoundException(MessageFormat.format("Could not find attachment {0} on document {1}", attachmentName, id)));
		
		try {
			return Response.ok()
				.entity(att.getData())
				.type(att.getContentType())
				.header(HttpHeaders.CONTENT_LENGTH, att.getLength())
				.header(HttpHeaders.LAST_MODIFIED, Instant.ofEpochMilli(att.getLastModified()))
				.tag(att.getETag())
				.build();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	@Path("{id}")
	@DELETE
	@Controller
	@RolesAllowed("login")
	public String delete(@PathParam("id") String id) {
		personRepository.deleteById(id);
		return "redirect:nosql/list";
	}
	
	// TODO figure out why Krazo's filter doesn't direct to the above
	@Path("{id}/delete")
	@POST
	@Controller
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public String deletePost(@PathParam("id") String id) {
		return delete(id);
	}
	
	@Path("{id}/update")
	@PATCH
	@Controller
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public String update(
			@PathParam("id") String id,
			@FormParam("firstName") String firstName,
			@FormParam("lastName") String lastName,
			@FormParam("birthday") String birthday,
			@FormParam("favoriteTime") String favoriteTime,
			@FormParam("added") String added
	) {
		Person person = personRepository.findById(id).get();
		person.setFirstName(firstName);
		person.setLastName(lastName);
		if(StringUtil.isNotEmpty(birthday)) {
			LocalDate bd = LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(birthday));
			person.setBirthday(bd);
		} else {
			person.setBirthday(null);
		}
		if(StringUtil.isNotEmpty(favoriteTime)) {
			LocalTime bd = LocalTime.from(DateTimeFormatter.ISO_LOCAL_TIME.parse(favoriteTime));
			person.setFavoriteTime(bd);
		} else {
			person.setFavoriteTime(null);
		}
		if(StringUtil.isNotEmpty(added)) {
			LocalDateTime dt = LocalDateTime.from(DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(added));
			Instant bd = dt.toInstant(ZoneOffset.UTC);
			person.setAdded(bd);
		} else {
			person.setAdded(null);
		}
		personRepository.save(person);
		return "redirect:nosql/list";
	}
	
	@Path("{id}/update")
	@POST
	@Controller
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public String updatePost(
			@PathParam("id") String id,
			@FormParam("firstName") String firstName,
			@FormParam("lastName") String lastName,
			@FormParam("birthday") String birthday,
			@FormParam("favoriteTime") String favoriteTime,
			@FormParam("added") String added
	) {
		return update(id, firstName, lastName, birthday, favoriteTime, added);
	}
	
	@Path("{id}")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Person updateJson(
			@PathParam("id") String id,
			Person person
	) {
		personRepository.findById(id)
			.orElseThrow(() -> new NotFoundException("Could not find Person for ID " + id));
		person.setUnid(id);
		return personRepository.save(person);
	}
	
	@Path("{id}/putInFolder")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public boolean putInFolder(@PathParam("id") String id) {
		Person person = personRepository.findById(id)
			.orElseThrow(() -> new NotFoundException("Unable to find Person for ID " + id));
		personRepository.putInFolder(person, PersonRepository.FOLDER_PERSONS);
		return true;
	}
	
	@Path("{id}/removeFromFolder")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public boolean removeFromFolder(@PathParam("id") String id) {
		Person person = personRepository.findById(id)
			.orElseThrow(() -> new NotFoundException("Unable to find Person for ID " + id));
		personRepository.removeFromFolder(person, PersonRepository.FOLDER_PERSONS);
		return true;
	}
	
	@Path("byViewKey/{lastName}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Person getPersonByViewKey(@PathParam("lastName") String lastName) {
		ViewQuery query = ViewQuery.query().key(lastName, true);
		return personRepository.findByKey(query)
			.orElseThrow(() -> new NotFoundException("Unable to find Person for last name: " + lastName));
	}
	
	@Path("byViewKeyMulti/{lastName}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Person> getPersonByViewKeyMulti(@PathParam("lastName") String lastName) {
		ViewQuery query = ViewQuery.query().key(lastName, true);
		return personRepository.findByKeyMulti(query, null, null).collect(Collectors.toList());
	}
	
	@Path("byViewTwoKeys/{lastName}/{firstName}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Person getPersonByViewTwoKey(@PathParam("lastName") String lastName, @PathParam("firstName") String firstName) {
		ViewQuery query = ViewQuery.query().key(Arrays.asList(lastName, firstName), true);
		return personRepository.findByKey(query)
			.orElseThrow(() -> new NotFoundException("Unable to find Person for last name: " + lastName));
	}
	
	@Path("byNoteId/{noteId}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Person getPersonByNoteID(@PathParam("noteId") String noteId) {
		return personRepository.findByNoteId(noteId)
			.orElseThrow(() -> new NotFoundException("Unable to find Person for note ID: " + noteId));
	}
	
	@Path("byNoteIdInt/{noteId}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Person getPersonByNoteIDInt(@PathParam("noteId") int noteId) {
		return personRepository.findByNoteId(noteId)
			.orElseThrow(() -> new NotFoundException("Unable to find Person for note ID: " + noteId));
	}
	
	@Path("modifiedSince/{modified}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Person> getPersonByModified(@PathParam("modified") String modified) {
		Instant mod = Instant.from(DateTimeFormatter.ISO_INSTANT.parse(modified));
		return personRepository.findModifiedSince(mod).collect(Collectors.toList());
	}
	
	@Path("findCategorized/{lastName}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Person> getCategorized(@PathParam("lastName") String lastName) {
		ViewQuery query = ViewQuery.query().key(lastName, true);
		return personRepository.findCategorized(query).collect(Collectors.toList());
	}
	
	@Path("findCategorizedManual/{lastName}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Person> getCategorizedManual(@PathParam("lastName") String lastName) {
		ViewQuery query = ViewQuery.query().key(lastName, true);
		return personRepository.readViewDocuments(PersonRepository.VIEW_PERSONS_CAT, -1, false, query, null, null)
			.collect(Collectors.toList());
	}
	
	@Path("findCategorizedDistinct/{lastName}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Person> getCategorizedDistinct(@PathParam("lastName") String lastName) {
		ViewQuery query = ViewQuery.query().key(lastName, true);
		return personRepository.findCategorizedDistinct(query).collect(Collectors.toList());
	}
	
	@Path("listViews")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<ViewInfo> listViews() {
		return personRepository.getViewInfo().collect(Collectors.toList());
	}
	
	/**
	 * @see <a href="https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/463">Issue #463</a>
	 */
	@Path("queryByEmail")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Person> queryByEmail(@QueryParam("q") @NotEmpty String searchValue) {
		ViewQuery query = ViewQuery.query().key(searchValue, true);
		return personRepository.readViewDocuments("PersonEmail", -1, false, query, null, null).collect(Collectors.toList());
	}
	
	/**
	 * @see <a href="https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/463">Issue #463</a>
	 */
	@Path("queryByEmailOneKey")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Person> queryByEmailOneKey(@QueryParam("q") @NotEmpty String searchValue, @QueryParam("resort") boolean resort) {
		ViewQuery query = ViewQuery.query().key(searchValue, true);
		Sort<Person> sorts = resort ? Sort.asc("email") : null;
		return personRepository.readViewDocuments("PersonEmailOneKey", -1, false, query, sorts, null).collect(Collectors.toList());
	}
	
	/**
	 * @see <a href="https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/463">Issue #463</a>
	 */
	@Path("queryByEmailEntries")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Person> queryByEmailEntries(@QueryParam("q") @NotEmpty String searchValue) {
		ViewQuery query = ViewQuery.query().key(searchValue, true);
		return personRepository.readViewEntries("PersonEmail", -1, false, query, null, null).collect(Collectors.toList());
	}
	
	private void composePerson(Person person, String firstName, String lastName, String birthday, String favoriteTime, String added, String customProperty, String email) {
		person.setFirstName(firstName);
		person.setLastName(lastName);
		if(StringUtil.isNotEmpty(birthday)) {
			LocalDate bd = LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE.parse(birthday));
			person.setBirthday(bd);
		} else {
			person.setBirthday(null);
		}
		if(StringUtil.isNotEmpty(favoriteTime)) {
			LocalTime bd = LocalTime.from(DateTimeFormatter.ISO_LOCAL_TIME.parse(favoriteTime));
			person.setFavoriteTime(bd);
		} else {
			person.setFavoriteTime(null);
		}
		if(StringUtil.isNotEmpty(added)) {
			LocalDateTime dt = LocalDateTime.from(DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(added));
			Instant bd = dt.toInstant(ZoneOffset.UTC);
			person.setAdded(bd);
		} else {
			person.setAdded(null);
		}
		person.setEmail(email);
		
		CustomPropertyType prop = new CustomPropertyType();
		prop.setValue(customProperty);
		person.setCustomProperty(prop);
	}
}
