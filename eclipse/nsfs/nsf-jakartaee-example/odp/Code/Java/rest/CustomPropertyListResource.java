package rest;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import model.CustomPropertyListEntity;

@Path("nosql/customPropertyList")
public class CustomPropertyListResource {
	@Inject
	private CustomPropertyListEntity.Repository entities;
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public CustomPropertyListEntity post(@Valid CustomPropertyListEntity entity) {
		return entities.save(entity);
	}
	
	@Path("{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public CustomPropertyListEntity get(@PathParam("id") String id) {
		return entities.findById(id)
			.orElseThrow(() -> new NotFoundException());
	}
}
