/**
 * Copyright © 2018-2022 Jesse Gallagher
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
package org.openntf.xsp.mvc.jaxrs;

import org.eclipse.krazo.engine.Viewable;
import org.eclipse.krazo.jaxrs.JaxRsContext;
import org.openntf.xsp.jaxrs.exceptions.GenericThrowableMapper;
import org.openntf.xsp.jaxrs.exceptions.NotFoundMapper;
import org.openntf.xsp.mvc.impl.DelegatingExceptionViewEngine;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.RequestScoped.Literal;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Inject;
import jakarta.mvc.Controller;
import jakarta.mvc.Models;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Priority(Priorities.USER+1)
public class MvcGenericThrowableMapper extends GenericThrowableMapper {
	
	@Context
	Request request;
	
	@Override
	public Response toResponse(Throwable t) {
		ResourceInfo resourceInfo = CDI.current().select(ResourceInfo.class, Literal.INSTANCE).get();
		HttpServletRequest req = CDI.current().select(HttpServletRequest.class).get();
		UriInfo uriInfo = CDI.current().select(UriInfo.class, Literal.INSTANCE).get();
		
		Response sup;
		// Depending on the container, this may be called for exceptions better handled by more-specialized classes
		if(t instanceof NotFoundException) {
			sup = NotFoundMapper.INSTANCE.toResponse((NotFoundException)t, request, uriInfo);
		} else if (t instanceof WebApplicationException) {
			WebApplicationException e = (WebApplicationException) t;
			if (e.getResponse() != null) {
				sup = e.getResponse();
			} else {
				sup = createResponseFromException(t, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, resourceInfo, req);
			}
		} else {
			sup = createResponseFromException(t, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, resourceInfo, req);
		}
		
		if(isMvcRequest(resourceInfo)) {
			Models models = CDI.current().select(Models.class).get();
			models.put("response", sup); //$NON-NLS-1$
			return Response.status(sup.getStatus())
				.type(sup.getMediaType())
				.encoding("UTF-8") //$NON-NLS-1$
				.entity(new Viewable(DelegatingExceptionViewEngine.class.getName(), DelegatingExceptionViewEngine.class))
				.build();
		} else {
			return sup;
		}
	}
	
	@Override
	protected MediaType getMediaType(ResourceInfo resourceInfo) {
		if(isMvcRequest(resourceInfo)) {
			Produces produces = resourceInfo.getResourceMethod().getAnnotation(Produces.class);
			if(produces != null) {
				return MediaType.valueOf(produces.value()[0]);
			} else {
				return MediaType.TEXT_HTML_TYPE;
			}
		} else {
			return super.getMediaType(resourceInfo);
		}
	}

	private boolean isMvcRequest(ResourceInfo resourceInfo) {
		if(resourceInfo == null) {
			return false;
		}
		if(resourceInfo.getResourceClass() != null && resourceInfo.getResourceClass().isAnnotationPresent(Controller.class)) {
			return true;
		}
		if(resourceInfo.getResourceMethod() != null && resourceInfo.getResourceMethod().isAnnotationPresent(Controller.class)) {
			return true;
		}
		return false;
	}
}
