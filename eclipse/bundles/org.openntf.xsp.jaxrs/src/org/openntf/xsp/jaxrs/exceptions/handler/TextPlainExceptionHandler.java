package org.openntf.xsp.jaxrs.exceptions.handler;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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

	@Override
	public boolean canHandle(ResourceInfo resourceInfo, MediaType mediaType) {
		return MediaType.TEXT_PLAIN_TYPE.isCompatible(mediaType) && !(mediaType.isWildcardType() || mediaType.isWildcardSubtype());
	}

	@Override
	public Response handle(Throwable throwable, int status, ResourceInfo resourceInfo, HttpServletRequest req) {
		return Response.status(status)
			.type(MediaType.TEXT_PLAIN)
			.entity((StreamingOutput)out -> {
				try(PrintWriter w = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
					throwable.printStackTrace(w);
				}
			})
			.build();
	}

}
