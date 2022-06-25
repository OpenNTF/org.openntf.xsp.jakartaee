package rest;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import model.ExampleDoc;
import model.ExampleDocRepository;

@Path("exampleDocs")
public class NoSQLExampleDocs {
	@Inject
	private ExampleDocRepository repository;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<ExampleDoc> get() {
		return repository.findAll().collect(Collectors.toList());
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public ExampleDoc create(@FormParam("title") String title, @FormParam("categories") List<String> categories, @FormParam("authors") List<String> authors) {
		ExampleDoc exampleDoc = new ExampleDoc();
		exampleDoc.setTitle(title);
		exampleDoc.setCategories(categories);
		exampleDoc.setAuthors(authors);
		return repository.save(exampleDoc, true);
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ExampleDoc createJson(ExampleDoc exampleDoc) {
		return repository.save(exampleDoc);
	}
	
	@Path("{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public ExampleDoc getDoc(@PathParam("id") String id) {
		return repository.findById(id)
			.orElseThrow(() -> new NotFoundException("Could not find example doc for ID " + id));
	}
}
