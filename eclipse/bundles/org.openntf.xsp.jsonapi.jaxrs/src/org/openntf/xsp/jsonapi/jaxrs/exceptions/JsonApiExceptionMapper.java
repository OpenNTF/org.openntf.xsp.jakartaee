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

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;

import org.openntf.xsp.jaxrs.ext.JsonExceptionMapper;

import jakarta.json.Json;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonGeneratorFactory;
import jakarta.ws.rs.core.Response.Status;

/**
 * @author Jesse Gallagher
 * @since 2.2.0
 */
public class JsonApiExceptionMapper implements JsonExceptionMapper {
	
	@Override
	public void writeJsonException(OutputStream os, Throwable throwable, String message) {
		JsonGeneratorFactory jsonFac = Json.createGeneratorFactory(Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true));
		try(JsonGenerator json = jsonFac.createGenerator(os)) {
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
	}
	
	@Override
	public void writeNotFoundException(OutputStream os, Throwable throwable) {
		JsonGeneratorFactory jsonFac = Json.createGeneratorFactory(Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true));
		try(JsonGenerator json = jsonFac.createGenerator(os)) {
			json.writeStartObject();
			json.write("success", false); //$NON-NLS-1$
			json.write("status", Status.NOT_FOUND.getStatusCode()); //$NON-NLS-1$
			json.write("errorMessage", throwable.toString()); //$NON-NLS-1$
			json.writeEnd();
		}
	}

}
