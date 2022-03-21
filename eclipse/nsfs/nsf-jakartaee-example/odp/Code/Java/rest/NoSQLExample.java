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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.ibm.commons.util.StringUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.Instant;
import java.time.format.DateTimeFormatter;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.mvc.Controller;
import jakarta.mvc.Models;
import jakarta.nosql.mapping.Sorts;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
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
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Object get(@QueryParam("lastName") String lastName) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("byQueryLastName", personRepository.findByLastName(lastName).collect(Collectors.toList()));
		result.put("totalCount", personRepository.count());
		return result;
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
			@FormParam("firstName") String firstName,
			@FormParam("lastName") String lastName,
			@FormParam("birthday") String birthday,
			@FormParam("favoriteTime") String favoriteTime,
			@FormParam("added") String added
	) {
		Person person = new Person();
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
	
	@Path("{id}")
	@GET
	@Controller
	public String show(@PathParam("id") String id) {
		models.put("person", personRepository.findById(id).get());
		return "person-show.jsp";
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
}
