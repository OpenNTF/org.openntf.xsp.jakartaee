package org.openntf.xsp.jakarta.mvc.rest;

import org.eclipse.krazo.security.CsrfExceptionMapper;
import org.openntf.xsp.jakarta.rest.exceptions.GenericThrowableMapper;

import jakarta.annotation.Priority;
import jakarta.mvc.security.CsrfValidationException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

/**
 * This variant of the Krazo CsrfExceptionMapper takes higher priority than it
 * in order to provide a response to the client - that version leads Resteasy
 * to send no HTML in the response, making it very unclear what the problem
 * is.
 * 
 * @since 3.5.0
 */
@Priority(Priorities.USER + 5001 - 2)
public class DominoCsrfExceptionMapper implements ExceptionMapper<CsrfValidationException> {
	private final GenericThrowableMapper delegate = new GenericThrowableMapper();
	private final CsrfExceptionMapper krazoDelegate = new CsrfExceptionMapper();
	
	@Override
	public Response toResponse(CsrfValidationException exception) {
		WebApplicationException wrapper = new WebApplicationException(exception, krazoDelegate.toResponse(exception));
		return delegate.toResponse(wrapper);
	}

}
