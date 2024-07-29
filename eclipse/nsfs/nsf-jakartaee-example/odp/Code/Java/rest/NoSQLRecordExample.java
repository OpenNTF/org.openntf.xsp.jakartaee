package rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import model.RecordExample;

@Path("nosqlRecordDocs")
public class NoSQLRecordExample {
	@Inject
	private RecordExample.Repository repository;
	
	@Path("byName/{name}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public RecordExample getByKey(@PathParam("name") String name) {
		return repository.findByName(name.replace('+', ' '))
			.orElseThrow(() -> new NotFoundException("No record found for name " + name));
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public RecordExample create(RecordExample entity) {
		return repository.insert(entity);
	}
}
