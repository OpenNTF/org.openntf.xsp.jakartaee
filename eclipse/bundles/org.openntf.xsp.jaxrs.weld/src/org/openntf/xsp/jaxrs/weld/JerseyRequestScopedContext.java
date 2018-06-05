package org.openntf.xsp.jaxrs.weld;

import org.glassfish.jersey.process.internal.RequestScoped;
import org.openntf.xsp.cdi.context.AbstractIdentifiedContext;

public class JerseyRequestScopedContext extends AbstractIdentifiedContext {
	public JerseyRequestScopedContext(String contextId) {
		super(contextId, null, RequestScoped.class);
	}
}
