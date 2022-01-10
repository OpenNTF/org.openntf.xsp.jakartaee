package org.openntf.xsp.jaxrs.security;

import java.io.IOException;
import java.lang.reflect.Method;

import jakarta.annotation.Priority;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

/**
 * Checks incoming requests for security annotations and enforces them using
 * the current security context.
 * 
 * @author Jesse Gallagher
 * @since 2.2.0
 */
@Provider
@Priority(Priorities.AUTHORIZATION-1)
public class RolesAllowedFilter implements ContainerRequestFilter {
	@Context
	ResourceInfo resourceInfo;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		if(!isAllowed(requestContext)) {
			ResponseBuilder response = Response.status(Response.Status.UNAUTHORIZED);
			requestContext.abortWith(response.build());
		}
	}
	
	private boolean isAllowed(ContainerRequestContext requestContext) {
		Method method = resourceInfo.getResourceMethod();
		Class<?> clazz = resourceInfo.getResourceClass();
		
		if(method.isAnnotationPresent(PermitAll.class)) {
			return true;
		}
		if(method.isAnnotationPresent(DenyAll.class)) {
			return false;
		}
		
		RolesAllowed roles = method.getAnnotation(RolesAllowed.class);
		if(roles == null) {
			roles = clazz.getAnnotation(RolesAllowed.class);
		}
		if(roles != null) {
			SecurityContext sec = requestContext.getSecurityContext();
			for(String role : roles.value()) {
				if(sec.isUserInRole(role)) {
					return true;
				}
			}
			return false;
		}
		
		return true;
	}

}
