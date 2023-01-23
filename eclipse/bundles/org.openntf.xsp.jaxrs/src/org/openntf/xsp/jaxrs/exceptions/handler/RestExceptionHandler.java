package org.openntf.xsp.jaxrs.exceptions.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * This service interface defines handlers for REST exceptions based on the
 * determined type of the endpoint and request.
 * 
 * <p>Handlers of this type can be prioritied with the
 * {@link jakarta.annotation.Priority Priority} annotation, with higher values
 * taking precende over lower values.</p>
 * 
 * @author Jesse Gallagher
 * @since 2.10.0
 */
public interface RestExceptionHandler {
	int DEFAULT_PRIORITY = 0;
	
	boolean canHandle(ResourceInfo resourceInfo, MediaType mediaType);
	
	Response handle(final Throwable throwable, final int status, ResourceInfo resourceInfo, HttpServletRequest req);
}
