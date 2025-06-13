/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
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
package rest;

import java.net.URI;
import java.util.concurrent.ExecutionException;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import bean.RestClientBean;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.JsonObject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("jaxrsClient")
public class JaxRsClientExample {
	
	@Inject
	private RestClientBean restClientBean;

	@Inject @Named("java:comp/DefaultManagedExecutorService")
	private ManagedExecutorService exec;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject get() {
		return restClientBean.getJsonObjectViaClient();
	}
	
	@Path("exampleObject")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public RestClientBean.JsonExampleObject getExampleObject() {
		return restClientBean.getExampleObjectViaClient();
	}
	
	@Path("echoExampleObject")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public RestClientBean.JsonExampleObject echoExampleObject(RestClientBean.JsonExampleObject obj) {
		obj.setFoo(obj.getFoo() + " - return value");
		return obj;
	}
	
	@Path("roundTripEcho")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public RestClientBean.JsonExampleObject getRoundTripEcho() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest)facesContext.getExternalContext().getRequest();
		
		RestClientBean.JsonExampleObject foo = new RestClientBean.JsonExampleObject();
		foo.setFoo("sending from async");
		
		URI uri = URI.create(request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/");
		uri = uri.resolve(facesContext.getExternalContext().getRequestContextPath() + "/");
		uri = uri.resolve("xsp/app/jaxrsClient/echoExampleObject");
		
		Client client = ClientBuilder.newBuilder().build();
		WebTarget target = client.target(uri);
		Response response = target.request().post(Entity.json(foo));
		
		return response.readEntity(RestClientBean.JsonExampleObject.class);
	}
	
	@Path("roundTripEchoAsync")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public RestClientBean.JsonExampleObject getRoundTripEchoAsync() throws InterruptedException, ExecutionException {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest)facesContext.getExternalContext().getRequest();
		
		return exec.submit(() -> {
			RestClientBean.JsonExampleObject foo = new RestClientBean.JsonExampleObject();
			foo.setFoo("sending from async");
			
			URI uri = URI.create(request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/");
			uri = uri.resolve(facesContext.getExternalContext().getRequestContextPath() + "/");
			uri = uri.resolve("xsp/app/jaxrsClient/echoExampleObject");
			
			Client client = ClientBuilder.newBuilder().build();
			WebTarget target = client.target(uri);
			Response response = target.request().post(Entity.json(foo));
			
			return response.readEntity(RestClientBean.JsonExampleObject.class);
		}).get();
	}
	
	@Path("roundTripEchoDoubleAsync")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public JsonObject getRoundTripEchoDoubleAsync() throws InterruptedException, ExecutionException {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest)facesContext.getExternalContext().getRequest();
		
		return exec.submit(() -> {
			URI uri = URI.create(request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/");
			uri = uri.resolve(facesContext.getExternalContext().getRequestContextPath() + "/");
			uri = uri.resolve("xsp/app/jaxrsClient/roundTripEchoAsync");
			
			Client client = ClientBuilder.newBuilder().build();
			WebTarget target = client.target(uri);
			Response response = target.request().get();
			
			return response.readEntity(JsonObject.class);
		}).get();
	}
}
