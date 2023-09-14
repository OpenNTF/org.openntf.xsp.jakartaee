package org.openntf.xsp.jaxrs.exceptions;

import com.ibm.xsp.acl.NoAccessSignal;

import jakarta.annotation.Priority;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;

@Priority(Integer.MAX_VALUE)
public class ForbiddenHandler implements ExceptionMapper<ForbiddenException> {
	
	@Context
	private UriInfo uriInfo;

	@Override
	public Response toResponse(ForbiddenException exception) {
		throw new NoAccessSignal(exception, uriInfo.getRequestUri().toString());
	}

}
