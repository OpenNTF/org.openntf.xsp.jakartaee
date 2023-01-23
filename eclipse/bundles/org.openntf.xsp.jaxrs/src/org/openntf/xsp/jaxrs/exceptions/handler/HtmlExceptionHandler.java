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
package org.openntf.xsp.jaxrs.exceptions.handler;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import javax.servlet.ServletException;

import com.ibm.designer.runtime.domino.adapter.util.XSPErrorPage;

import jakarta.annotation.Priority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

/**
 * This handle will render exceptions using the server-default XPages
 * exception page when the media type is {@code text/html}.
 * 
 * @author Jesse Gallagher
 * @since 2.10.0
 */
@Priority(RestExceptionHandler.DEFAULT_PRIORITY)
public class HtmlExceptionHandler implements RestExceptionHandler {

	@Override
	public boolean canHandle(ResourceInfo resourceInfo, MediaType mediaType) {
		return MediaType.TEXT_HTML_TYPE.isCompatible(mediaType);
	}

	@Override
	public Response handle(Throwable throwable, int status, ResourceInfo resourceInfo, HttpServletRequest req) {
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
	}

}
