package org.openntf.xsp.jakarta.rest.filter;

import java.io.IOException;
import java.util.List;

import org.jboss.resteasy.util.AcceptParser;

import com.ibm.commons.util.StringUtil;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;

/**
 * @since 3.7.0
 */
@Provider
@Priority(Priorities.ENTITY_CODER)
public class CompressorResponseFilter implements ContainerResponseFilter {
	public static final String PROP_ENABLED = "rest.gzip.auto"; //$NON-NLS-1$
	
	@Context
	private Application application;

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		// Exit early if it's been disabled in the app properties
		if("false".equals(application.getProperties().get(PROP_ENABLED))) { //$NON-NLS-1$
			return;
		}
		
		// Also exit early if it's already encoded
		var existingEncoding = responseContext.getHeaderString(HttpHeaders.CONTENT_ENCODING);
		if(StringUtil.isNotEmpty(existingEncoding)) {
			return;
		}
		
		var accept = requestContext.getHeaderString(HttpHeaders.ACCEPT_ENCODING);
		List<String> accepts = AcceptParser.parseAcceptHeader(StringUtil.toString(accept));
		if(accepts.contains("gzip")) { //$NON-NLS-1$
			var type = responseContext.getMediaType();
			if(type != null) {
				if(shouldCompress(type)) {
					// Set the header and let RESTEasy's interceptor handle it
					responseContext.getHeaders().put(HttpHeaders.CONTENT_ENCODING, List.of("gzip")); //$NON-NLS-1$
				}
			}
		}
	}
	
	private boolean shouldCompress(MediaType type) {
		if("text".equals(type.getType())) { //$NON-NLS-1$
			return true;
		} else if(MediaType.APPLICATION_JSON_TYPE.isCompatible(type)) {
			return true;
		} else if(MediaType.APPLICATION_XML_TYPE.isCompatible(type)) {
			return true;
		} else if(type.getSubtype().endsWith("+xml")) { //$NON-NLS-1$
			return true;
		}
		return false;
	}

}
