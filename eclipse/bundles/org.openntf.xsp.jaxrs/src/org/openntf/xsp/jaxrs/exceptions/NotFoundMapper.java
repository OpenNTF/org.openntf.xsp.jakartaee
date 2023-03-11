/**
 * Copyright (c) 2018-2023 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jaxrs.exceptions;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import com.ibm.designer.runtime.domino.adapter.util.XSPErrorPage;

import jakarta.annotation.Priority;
import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonGeneratorFactory;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Variant;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;

/**
 * @author Jesse Gallagher
 * @since 2.2.0
 */
@Priority(Priorities.USER+1)
public class NotFoundMapper implements ExceptionMapper<NotFoundException> {
	public static final NotFoundMapper INSTANCE = new NotFoundMapper();
	
	@Context
	Request request;
	
	@Context
	UriInfo uriInfo;
	
	@Override
	public Response toResponse(final NotFoundException exception) {
		return toResponse(exception, request, uriInfo);
	}
	
	public Response toResponse(NotFoundException exception, Request request, UriInfo uriInfo) {
		List<Variant> options = Variant.mediaTypes(MediaType.APPLICATION_JSON_TYPE, MediaType.TEXT_HTML_TYPE).build();
		Variant preferred = request.selectVariant(options);
		
		if(MediaType.TEXT_HTML_TYPE.isCompatible(preferred.getMediaType())) {
			return Response.status(Status.NOT_FOUND)
				.type(MediaType.TEXT_HTML_TYPE)
				.entity((StreamingOutput)out -> {
					try(
						OutputStreamWriter outWriter = new OutputStreamWriter(out, StandardCharsets.UTF_8);
						PrintWriter w = new PrintWriter(outWriter)
					) {
						XSPErrorPage.handlePageNotFound(w, uriInfo.getRequestUri().toString(), exception, null, false);
					}
				})
				.build();
				
		} else {
			return Response.status(Status.NOT_FOUND)
				.type(MediaType.APPLICATION_JSON_TYPE)
				.entity((StreamingOutput)out -> {
					JsonGeneratorFactory jsonFac = Json.createGeneratorFactory(Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true));
					try(JsonGenerator json = jsonFac.createGenerator(out)) {
						json.writeStartObject();
						json.write("success", false); //$NON-NLS-1$
						json.write("status", Status.NOT_FOUND.getStatusCode()); //$NON-NLS-1$
						json.write("errorMessage", exception.toString()); //$NON-NLS-1$
						json.writeEnd();
					}
				})
				.build();
		}
	}
}
