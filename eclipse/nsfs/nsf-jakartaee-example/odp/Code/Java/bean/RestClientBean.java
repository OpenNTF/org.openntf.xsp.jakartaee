/**
 * Copyright © 2018-2022 Contributors to the XPages Jakarta EE Support Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bean;

import java.net.URI;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.openntf.xsp.jsonapi.JSONBindUtil;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.json.bind.JsonbBuilder;
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
	
	public Object getObjectViaRest() {
		HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest(); 
		URI uri = URI.create(request.getRequestURL().toString());
		URI serviceUri = uri.resolve("xsp/app/jsonExample");
		JsonExampleService service = RestClientBuilder.newBuilder()
			.baseUri(serviceUri)
			.build(JsonExampleService.class);
		return JSONBindUtil.toJson(service.get(), JsonbBuilder.create());
	}
}
