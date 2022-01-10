package org.openntf.xsp.jsonapi.jaxrs.exceptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import jakarta.annotation.Priority;
import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonGeneratorFactory;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.ext.ExceptionMapper;
import lotus.domino.NotesException;

/**
 * @author Jesse Gallagher
 * @since 2.2.0
 */
@Priority(Priorities.USER+1)
public class GenericThrowableMapper implements ExceptionMapper<Throwable> {

	@Override
	public Response toResponse(final Throwable t) {
		// Depending on the container, this may be called for exceptions better handled by more-specialized classes
		if(t instanceof NotFoundException) {
			return NotFoundMapper.INSTANCE.toResponse((NotFoundException)t);
		}

		if (t instanceof WebApplicationException) {
			WebApplicationException e = (WebApplicationException) t;
			if (e.getResponse() != null) {
				return e.getResponse();
			} else {
				return createResponseFromException(t, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} else {
			return createResponseFromException(t, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private Response createResponseFromException(final Throwable throwable, final int status) {
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
