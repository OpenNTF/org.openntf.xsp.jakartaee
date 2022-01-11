package rest;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.mvc.Controller;
import jakarta.mvc.Models;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;

@Path("mvc")
@Controller
@Dependent
public class MvcExample {
	
	@Inject
	Models models;
	
	@Inject
	@Named("sessionAsSigner")
	Session session;
	
	@Inject
	Database database;
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String get(@QueryParam("foo") String foo) throws NotesException {
		models.put("incomingFoo", foo);
		models.put("contextFromController", "s: " + session.isRestricted() + "; db: " + database);
		return "mvc.jsp";
	}
}