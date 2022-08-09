/**
 * Copyright © 2018-2022 Contributors to the XPages Jakarta EE Support Project
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jnosql.communication.driver.attachment.EntityAttachment;
import org.openntf.xsp.nosql.communication.driver.ByteArrayEntityAttachment;

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
import jakarta.nosql.mapping.Sorts;
import jakarta.transaction.UserTransaction;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
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
		result.put("totalCount", personRepository.count());
		return result;
	}
	
	@Path("inFolder")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Person> getInFolder() {
		return personRepository.findInPersonsFolder().collect(Collectors.toList());
	}
	
	@Path("servers")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Object getServers() {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("all", serverRepository.findAll(Sorts.sorts().asc("serverName")).collect(Collectors.toList()));
		result.put("totalCount", serverRepository.count());
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
			@FormParam("customProperty") String customProperty
	) throws Exception {
		transaction.begin();
		try {
			Person person = new Person();
			composePerson(person, firstName, lastName, birthday, favoriteTime, added, customProperty);
			
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
			composePerson(person, firstName, lastName, birthday, favoriteTime, added, customProperty);
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
		composePerson(person, firstName, lastName, birthday, favoriteTime, added, customProperty);
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
			models.put("persons", personRepository.findAll(Sorts.sorts().asc(sortCol)).collect(Collectors.toList()));
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
			return personRepository.findAll(Sorts.sorts().asc(sortCol)).collect(Collectors.toList());
		}
	}
	
	@Path("{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Person getPerson(@PathParam("id") String id) {
		return personRepository.findById(id)
			.orElseThrow(() -> new NotFoundException("Unable to find Person for ID " + id));
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
		return personRepository.findByKey(lastName)
			.orElseThrow(() -> new NotFoundException("Unable to find Person for last name: " + lastName));
	}
	
	@Path("byViewKeyMulti/{lastName}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Person> getPersonByViewKeyMulti(@PathParam("lastName") String lastName) {
		return personRepository.findByKeyMulti(lastName).collect(Collectors.toList());
	}
	
	@Path("byViewTwoKeys/{lastName}/{firstName}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Person getPersonByViewTwoKey(@PathParam("lastName") String lastName, @PathParam("firstName") String firstName) {
		return personRepository.findByTwoKeys(lastName, firstName)
			.orElseThrow(() -> new NotFoundException("Unable to find Person for last name: " + lastName));
	}
	
	@Path("byViewCollectionKey/{lastName}/{firstName}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Person getPersonByViewCollectionKey(@PathParam("lastName") String lastName, @PathParam("firstName") String firstName) {
		return personRepository.findByCollection(Arrays.asList(lastName, firstName))
			.orElseThrow(() -> new NotFoundException("Unable to find Person for last name: " + lastName));
	}
	
	@Path("byNoteId/{noteId}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Person getPersonByNoteID(@PathParam("noteId") String noteId) {
		return personRepository.findByNoteId(noteId)
			.orElseThrow(() -> new NotFoundException("Unable to find Person for note ID: " + noteId));
	}
	
	@Path("byModified/{modified}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Person> getPersonByModified(@PathParam("modified") String modified) {
		Instant mod = Instant.from(DateTimeFormatter.ISO_INSTANT.parse(modified));
		return personRepository.findByModified(mod).collect(Collectors.toList());
	}
	
	private void composePerson(Person person, String firstName, String lastName, String birthday, String favoriteTime, String added, String customProperty) {
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
		
		CustomPropertyType prop = new CustomPropertyType();
		prop.setValue(customProperty);
		person.setCustomProperty(prop);
	}
}
