/**
 * Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
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

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.charset.StandardCharsets;

import jakarta.annotation.Priority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

/**
 * This handle will render exceptions as just their stack trace when the
 * media type is {@code text/plain}.
 *
 * @author Jesse Gallagher
 * @since 2.10.0
 */
@Priority(RestExceptionHandler.DEFAULT_PRIORITY)
public class TextPlainExceptionHandler implements RestExceptionHandler {
	private static final Logger log = System.getLogger(TextPlainExceptionHandler.class.getName());

	@Override
	public boolean canHandle(final ResourceInfo resourceInfo, final MediaType mediaType) {
		return MediaType.TEXT_PLAIN_TYPE.isCompatible(mediaType) && !(mediaType.isWildcardType() || mediaType.isWildcardSubtype());
	}

	@Override
	public Response handle(final Throwable throwable, final int status, final ResourceInfo resourceInfo, final HttpServletRequest req) {
		return Response.status(status)
			.type(MediaType.TEXT_PLAIN)
			.entity((StreamingOutput)out -> {
				try(PrintWriter w = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
					throwable.printStackTrace(w);
				} catch(Throwable t) {
					if(log.isLoggable(Level.ERROR)) {
						log.log(Level.ERROR, "Encountered exception writing text exception output", t);
						log.log(Level.ERROR, "Original exception", throwable);
					}
					throw t;
				}
			})
			.build();
	}

}
