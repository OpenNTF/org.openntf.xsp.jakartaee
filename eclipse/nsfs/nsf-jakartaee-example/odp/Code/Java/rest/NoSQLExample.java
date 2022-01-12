package rest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import model.Person;
import model.PersonRepository;

@Path("nosql")
public class NoSQLExample {
	@Inject
	PersonRepository personRepository;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Object get(@QueryParam("lastName") String lastName) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put("byQueryLastName", personRepository.findByLastName(lastName).collect(Collectors.toList()));
		result.put("totalCount", personRepository.count());
		return result;
	}
}
