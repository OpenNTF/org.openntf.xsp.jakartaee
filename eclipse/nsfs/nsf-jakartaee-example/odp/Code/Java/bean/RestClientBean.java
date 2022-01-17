package bean;

import java.net.URI;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.microprofile.rest.client.RestClientBuilder;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@RequestScoped
@Named("restClientBean")
public class RestClientBean {
	public static class JsonExampleObject {
		private String foo;
		
		public String getFoo() {
			return foo;
		}
		public void setFoo(String foo) {
			this.foo = foo;
		}
	}
	
	public interface JsonExampleService {
		@GET
		@Produces(MediaType.APPLICATION_JSON)
		JsonExampleObject get();
	}
	
	public JsonExampleObject getObjectViaRest() {
		HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest(); 
		URI uri = URI.create(request.getRequestURL().toString());
		URI serviceUri = uri.resolve("jsonExample");
		JsonExampleService service = RestClientBuilder.newBuilder()
			.baseUri(serviceUri)
			.build(JsonExampleService.class);
		return service.get();
	}
}
