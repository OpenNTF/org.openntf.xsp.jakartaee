package org.openntf.xsp.cdi.impl;

import java.lang.annotation.Annotation;

import org.jboss.weld.contexts.AbstractSharedContext;

public class AbstractIdentifiedContext extends AbstractSharedContext {
	private final String id;
	private final Class<? extends Annotation> scope;
	private boolean active = true;

	public AbstractIdentifiedContext(String contextId, String id, Class<? extends Annotation> scope) {
		super(contextId);
		this.id = id;
		this.scope = scope;
	}

	@Override
	public Class<? extends Annotation> getScope() {
		return scope;
	}
	
	public String getId() {
		return id;
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
		this.active = false;
	}
	
	@Override
	public boolean isActive() {
		return active;
	}

}
