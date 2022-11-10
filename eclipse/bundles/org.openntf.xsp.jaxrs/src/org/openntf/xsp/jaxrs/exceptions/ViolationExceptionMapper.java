package org.openntf.xsp.jaxrs.exceptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jboss.resteasy.api.validation.ResteasyViolationException;
import org.jboss.resteasy.plugins.validation.ResteasyViolationExceptionMapper;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.Provider;

/**
 * This subclass of {@link ResteasyViolationExceptionMapper} uses the
 * {@link Produces @Produces} annotation of the target method, when present,
 * in preference to the client's {@code Accept} header.
 * 
 * @author Jesse Gallagher
 * @since 2.9.0
 */
@Provider
public class ViolationExceptionMapper extends ResteasyViolationExceptionMapper {

	@Context
	private ResourceInfo resourceInfo;
	
	@Override
	protected Response buildViolationReportResponse(ResteasyViolationException exception, Status status) {
		if(resourceInfo != null) {
			Produces produces = resourceInfo.getResourceMethod().getAnnotation(Produces.class);
			if(produces != null) {
				List<MediaType> accept = new ArrayList<>();
				Arrays.stream(produces.value())
					.map(MediaType::valueOf)
					.forEach(accept::add);
				accept.addAll(exception.getAccept());
				exception.setAccept(accept);
			}
		}
		return super.buildViolationReportResponse(exception, status);
	}
}
