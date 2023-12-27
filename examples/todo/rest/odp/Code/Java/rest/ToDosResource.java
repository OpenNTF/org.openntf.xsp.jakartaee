package rest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.nosql.mapping.Sorts;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import model.ToDo;

@Path("todos")
public class ToDosResource {
	@Inject
	private ToDo.Repository repository;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<ToDo> get(@QueryParam("status") ToDo.State status) {
		if(status == null) {
			return repository.findAll(Sorts.sorts().asc("created")).collect(Collectors.toList());
		} else {
			return repository.findByStatus(status, Sorts.sorts().asc("created")).collect(Collectors.toList());
		}
	}
	
	@Path("{documentId}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public ToDo getToDo(@PathParam("documentId") String documentId) {
		return repository.findById(documentId)
			.orElseThrow(() -> new NotFoundException());
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ToDo create(@Valid ToDo todo) {
		todo.setDocumentId(null);
		todo.setStatus(ToDo.State.Incomplete);
		todo.setCreated(OffsetDateTime.now());
		
		return repository.save(todo);
	}
	
	@Path("{documentId}/toggle")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ToDo put(@PathParam("documentId") String documentId) {
		ToDo todo = repository.findById(documentId)
			.orElseThrow(() -> new NotFoundException());
		todo.setStatus(todo.getStatus() == ToDo.State.Complete ? ToDo.State.Incomplete : ToDo.State.Complete);
		
		return repository.save(todo);
	}
	
	@Path("{documentId}")
	@PATCH
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ToDo patch(@PathParam("documentId") String documentId, @Valid ToDo todo) {
		ToDo existing = repository.findById(documentId)
			.orElseThrow(() -> new NotFoundException());
		existing.setStatus(todo.getStatus() == null ? ToDo.State.Incomplete : todo.getStatus());
		existing.setTitle(todo.getTitle());
		return repository.save(existing);
	}
	
	@Path("{documentId}")
	@DELETE
	public void delete(@PathParam("documentId") String documentId) {
		repository.deleteById(documentId);
	}
}
