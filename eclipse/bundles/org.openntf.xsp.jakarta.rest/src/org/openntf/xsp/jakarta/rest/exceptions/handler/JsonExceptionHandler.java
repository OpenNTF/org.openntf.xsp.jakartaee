/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.xsp.jakarta.rest.exceptions.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

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

	private static final Logger log = Logger.getLogger(JsonExceptionHandler.class.getName());

	@Override
	public boolean canHandle(final ResourceInfo resourceInfo, final MediaType mediaType) {
		return MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType);
	}

	@Override
	public Response handle(final Throwable throwable, final int status, final ResourceInfo resourceInfo, final HttpServletRequest req) {
		return Response.status(status)
			.type(MediaType.APPLICATION_JSON_TYPE)
			.entity((StreamingOutput)out -> {
				try {
					Objects.requireNonNull(out);
					String message = ""; //$NON-NLS-1$
					Throwable t = throwable;
					while ((message == null || message.length() == 0) && t != null) {
						if (t instanceof NotesException ne) {
							message = ne.text;
						} else if (t instanceof ConstraintViolationException cve) {
							message = t.getMessage();

							if (message == null || message.isEmpty()) {
								List<String> cvMsgList = new ArrayList<>();
								for (@SuppressWarnings("rawtypes")
								ConstraintViolation cv : cve.getConstraintViolations()) {
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
				} catch(Throwable t) {
					if(log.isLoggable(Level.SEVERE)) {
						log.log(Level.SEVERE, "Encountered exception writing JSON exception output", t);
						log.log(Level.SEVERE, "Original exception", throwable);
					}
					throw t;
				}
			})
			.build();
	}

}
