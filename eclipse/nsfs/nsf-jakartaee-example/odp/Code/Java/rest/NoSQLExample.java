package rest;

import java.util.List;
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
	PersonRepository persons;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Person> get(@QueryParam("lastName") String lastName) {
		return persons.findByLastName(lastName).collect(Collectors.toList());
	}
}
