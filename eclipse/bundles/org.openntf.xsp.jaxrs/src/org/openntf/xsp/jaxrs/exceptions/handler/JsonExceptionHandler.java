package org.openntf.xsp.jaxrs.exceptions.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import jakarta.annotation.Priority;
import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonGeneratorFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import lotus.domino.NotesException;

/**
 * This handle will render exceptions as JSON objects with stack elements
 * represented as an array of strings when the content type is
 * {@code application/json}.
 * 
 * <p>This is also used by the mapper as the generic fallback handler when
 * no other registered handler applies.</p>
 * 
 * @author Jesse Gallagher
 * @since 2.10.0
 */
@Priority(RestExceptionHandler.DEFAULT_PRIORITY)
public class JsonExceptionHandler implements RestExceptionHandler {
	public static final JsonExceptionHandler DEFAULT = new JsonExceptionHandler();

	@Override
	public boolean canHandle(ResourceInfo resourceInfo, MediaType mediaType) {
		return MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType);
	}

	@Override
	public Response handle(Throwable throwable, int status, ResourceInfo resourceInfo, HttpServletRequest req) {
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
