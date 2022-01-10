package org.openntf.xsp.jaxrs.security;

import java.io.IOException;

import jakarta.annotation.Priority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHORIZATION-1)
public class SecurityRequestFilter implements ContainerRequestFilter {
	@Context
	UriInfo uriInfo;
	
	@Context
	HttpServletRequest req;

	@Override
	public void filter(final ContainerRequestContext requestContext) throws IOException {
		requestContext.setSecurityContext(new JAXRSSecurityContext(req));
	}
}
