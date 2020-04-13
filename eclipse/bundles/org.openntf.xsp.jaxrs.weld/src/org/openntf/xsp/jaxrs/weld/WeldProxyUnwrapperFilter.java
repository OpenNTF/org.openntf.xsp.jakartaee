package org.openntf.xsp.jaxrs.weld;

import java.io.IOException;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import org.jboss.weld.proxy.WeldClientProxy;
import org.jboss.weld.proxy.WeldClientProxy.Metadata;

/**
 * This {@link ContainerResponseFilter} checks for outgoing Weld bean proxies and unwraps them,
 * which avoids trouble with many {@link javax.ws.rs.ext.MessageBodyWriter MessageBodyWriter}
 * that rely on reflection and annotations.
 * 
 * @author Jesse Gallagher
 * @since 1.2.0
 */
@Provider
@Priority(Priorities.ENTITY_CODER)
public class WeldProxyUnwrapperFilter implements ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		Object entity = responseContext.getEntity();
		if(entity instanceof WeldClientProxy) {
			Metadata metadata = ((WeldClientProxy)entity).getMetadata();
			responseContext.setEntity(metadata.getContextualInstance());
		}
	}
}
