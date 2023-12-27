package controller;

import jakarta.mvc.Controller;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("")
@Controller
public class HomeController {
	@GET
	@Produces(MediaType.TEXT_HTML)
	public String get() {
		return "home.jsp";
	}
}
