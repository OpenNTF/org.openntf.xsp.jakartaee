package controller;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

import com.ibm.commons.util.StringUtil;

import jakarta.inject.Inject;
import jakarta.mvc.Controller;
import jakarta.mvc.Models;
import jakarta.data.Sort;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import model.ToDo;

@Path("todos")
@Controller
public class ToDosController {
	@Inject
	private Models models;
	
	@Inject
	private ToDo.Repository repository;
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String get(@QueryParam("status") ToDo.State status) {
		if(status == null) {
			models.put("todos", repository.findAll(Sort.asc("created")).collect(Collectors.toList()));
		} else {
			models.put("todos", repository.findByStatus(status, Sort.asc("created")).collect(Collectors.toList()));
		}
		return "todos.jsp";
	}
	
	@Path("{documentId}")
	@GET
	public String getToDo(@PathParam("documentId") String documentId) {
		ToDo todo = repository.findById(documentId)
			.orElseThrow(() -> new NotFoundException());
		models.put("todo", todo);
		return "todo.jsp";
	}
	
	@Path("{documentId}/delete")
	@POST
	public String deleteToDo(@PathParam("documentId") String documentId) {
		repository.deleteById(documentId);
		return "redirect:todos";
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public String create(@NotEmpty @FormParam("title") String title) {
		ToDo todo = new ToDo();
		todo.setTitle(title);
		todo.setCreated(OffsetDateTime.now());
		todo.setStatus(ToDo.State.Incomplete);
		repository.save(todo);
		return "redirect:todos";
	}
	
	@Path("{documentId}")
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public String saveToDo(@PathParam("documentId") String documentId, @FormParam("title") @NotEmpty String title, @FormParam("status") String status) {
		ToDo todo = repository.findById(documentId)
			.orElseThrow(() -> new NotFoundException());
		todo.setTitle(title);
		todo.setStatus(StringUtil.isEmpty(status) ? ToDo.State.Incomplete : ToDo.State.valueOf(status));
		repository.save(todo);
		
		return "redirect:todos";
	}
}
