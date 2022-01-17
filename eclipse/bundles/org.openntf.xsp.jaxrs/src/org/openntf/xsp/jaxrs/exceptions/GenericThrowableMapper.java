package org.openntf.xsp.jaxrs.exceptions;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.servlet.ServletException;

import com.ibm.designer.runtime.domino.adapter.util.XSPErrorPage;

import jakarta.annotation.Priority;
import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonGeneratorFactory;
import jakarta.mvc.Controller;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import lotus.domino.NotesException;

/**
 * @author Jesse Gallagher
 * @since 2.3.0
 */
@Priority(Priorities.USER+2)
public class GenericThrowableMapper implements ExceptionMapper<Throwable> {

	@Context
	private UriInfo uriInfo;
	
	@Context
	private HttpServletRequest req;
	
	@Context
	private ResourceInfo resourceInfo;
	
	@Context
	private Request request;

	@Override
	public Response toResponse(final Throwable t) {
		// Depending on the container, this may be called for exceptions better handled by more-specialized classes
		if(t instanceof NotFoundException) {
			return NotFoundMapper.INSTANCE.toResponse((NotFoundException)t, request, uriInfo);
		}

		if (t instanceof WebApplicationException) {
			WebApplicationException e = (WebApplicationException) t;
			if (e.getResponse() != null) {
				return e.getResponse();
			} else {
				return createResponseFromException(t, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, resourceInfo, req);
			}
		} else {
			return createResponseFromException(t, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, resourceInfo, req);
		}
	}
	
	protected MediaType getMediaType(ResourceInfo resourceInfo) {
		if(resourceInfo == null) {
			return MediaType.APPLICATION_JSON_TYPE;
		}
		
		Method method = resourceInfo.getResourceMethod();
		if(method == null) {
			// Shows up currently with MVC, which breaks resolution
			return MediaType.APPLICATION_JSON_TYPE;
		}
		Produces produces = method.getAnnotation(Produces.class);
		if(produces != null) {
			// Assume the first is "true"
			return MediaType.valueOf(produces.value()[0]);
		} else if(isMvcRequest(resourceInfo)) {
			// An MVC request without a type should be assumed to be HTML
			return MediaType.TEXT_HTML_TYPE;
		} else {
			return MediaType.APPLICATION_JSON_TYPE;
		}
	}

	protected Response createResponseFromException(final Throwable throwable, final int status, ResourceInfo resourceInfo, HttpServletRequest req) {
		MediaType type = getMediaType(resourceInfo);
		if(MediaType.TEXT_HTML_TYPE.isCompatible(type)) {
			// Handle as HTML
			return Response.status(status)
				.type(MediaType.TEXT_HTML_TYPE)
				.entity((StreamingOutput)out -> {
					try(PrintWriter w = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
						XSPErrorPage.handleException(w, throwable, req.getRequestURL().toString(), false);
					} catch (ServletException e) {
						throw new IOException(e);
					}
				})
				.build();
		} else {
			// Handle as JSON
			return Response.status(status)
				.type(MediaType.APPLICATION_JSON_TYPE)
				.entity((StreamingOutput)out -> {
					Objects.requireNonNull(out);
					String message = ""; //$NON-NLS-1$
					Throwable t = throwable;
					while ((message == null || message.length() == 0) && t != null) {
						if (t instanceof NotesException) {
							message = ((NotesException) t).text;
						} else if (t instanceof ConstraintViolationException) {
							message = t.getMessage();

							if (message == null || message.isEmpty()) {
								List<String> cvMsgList = new ArrayList<>();
								for (@SuppressWarnings("rawtypes")
								ConstraintViolation cv : ((ConstraintViolationException) t).getConstraintViolations()) {
									String cvMsg = cv.getPropertyPath() + ": " + cv.getMessage(); //$NON-NLS-1$
									cvMsgList.add(cvMsg);
								}
								message = String.join(",", cvMsgList); //$NON-NLS-1$
							}
						} else {
							message = t.getMessage();
						}

						t = t.getCause();
					}
					
					JsonGeneratorFactory jsonFac = Json.createGeneratorFactory(Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true));
					try(JsonGenerator json = jsonFac.createGenerator(out)) {
						json.writeStartObject();
						
						json.write("message", throwable.getClass().getName() + ": " + message); //$NON-NLS-1$ //$NON-NLS-2$
						
						json.writeKey("stackTrace"); //$NON-NLS-1$
						json.writeStartArray();
						for (Throwable cause = throwable; cause != null; cause = cause.getCause()) {
							json.writeStartArray();
							json.write(cause.getClass().getName() + ": " + cause.getLocalizedMessage()); //$NON-NLS-1$
							Arrays.stream(cause.getStackTrace())
								.map(String::valueOf)
								.map(line -> "  at " + line) //$NON-NLS-1$
								.forEach(json::write);
							json.writeEnd();
						}
						json.writeEnd();
						
						json.writeEnd();
					}
				})
				.build();
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
