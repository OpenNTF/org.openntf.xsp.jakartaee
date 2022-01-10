/**
 * Copyright © 2018-2022 Jesse Gallagher
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
package org.openntf.xsp.jsonapi.jaxrs.exceptions;

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
