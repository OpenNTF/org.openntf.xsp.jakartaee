package rest;

import jakarta.inject.Inject;
import jakarta.mvc.Controller;
import jakarta.mvc.Models;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("mvcjsf")
@Controller
public class MvcJsfExample {
	
	@Inject
	private Models models;
	
	@GET
	public String get() {
		models.put("setByController", "I am a string set by the controller");
		return "jsfview.xhtml";
	}
}
