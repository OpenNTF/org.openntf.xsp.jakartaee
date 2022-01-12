package org.openntf.xsp.mvc.jaxrs;

import org.eclipse.krazo.engine.Viewable;
import org.openntf.xsp.jsonapi.jaxrs.exceptions.GenericThrowableMapper;
import org.openntf.xsp.mvc.impl.DelegatingExceptionViewEngine;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.mvc.Controller;
import jakarta.mvc.Models;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Priority(Priorities.USER+1)
public class MvcGenericThrowableMapper extends GenericThrowableMapper {

	@Context
	private ResourceInfo resourceInfo;
	
	@Override
	public Response toResponse(Throwable t) {
		Response sup = super.toResponse(t);
		if(isMvcRequest()) {
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
	protected MediaType getMediaType() {
		if(isMvcRequest()) {
			Produces produces = resourceInfo.getResourceMethod().getAnnotation(Produces.class);
			if(produces != null) {
				return MediaType.valueOf(produces.value()[0]);
			} else {
				return MediaType.TEXT_HTML_TYPE;
			}
		} else {
			return super.getMediaType();
		}
	}

	private boolean isMvcRequest() {
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
