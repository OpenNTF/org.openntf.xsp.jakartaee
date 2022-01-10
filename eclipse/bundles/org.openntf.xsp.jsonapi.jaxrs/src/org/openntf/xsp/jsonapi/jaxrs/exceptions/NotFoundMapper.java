package org.openntf.xsp.jsonapi.jaxrs.exceptions;

import java.io.PrintWriter;
import java.util.Collections;

import jakarta.annotation.Priority;
import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonGeneratorFactory;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.ext.ExceptionMapper;

/**
 * @author Jesse Gallagher
 * @since 2.2.0
 */
@Priority(Priorities.USER+1)
public class NotFoundMapper implements ExceptionMapper<NotFoundException> {
	public static final NotFoundMapper INSTANCE = new NotFoundMapper();
	
	@Override
	public Response toResponse(final NotFoundException exception) {
		return Response.status(HttpServletResponse.SC_NOT_FOUND)
			.type(MediaType.APPLICATION_JSON_TYPE)
			.entity((StreamingOutput)out -> {
				JsonGeneratorFactory jsonFac = Json.createGeneratorFactory(Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true));
				try(JsonGenerator json = jsonFac.createGenerator(out)) {
					json.writeStartObject();
					json.write("success", false); //$NON-NLS-1$
					json.write("status", HttpServletResponse.SC_NOT_FOUND); //$NON-NLS-1$
					json.write("errorMessage", exception.toString()); //$NON-NLS-1$
					json.writeEnd();
				}
			})
			.build();
	}
}
