package org.openntf.xsp.jaxrs.exceptions;

import com.ibm.xsp.acl.NoAccessSignal;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;

/**
 * @param <T> the type of exception handled by this class
 * @since 2.14.0
 */
public abstract class AbstractForbiddenExceptionHandler<T extends Exception> implements ExceptionMapper<T> {
	@Context
	private UriInfo uriInfo;

	@Override
	public Response toResponse(T exception) {
		throw new NoAccessSignal(exception, uriInfo.getRequestUri().toString());
	}
}
