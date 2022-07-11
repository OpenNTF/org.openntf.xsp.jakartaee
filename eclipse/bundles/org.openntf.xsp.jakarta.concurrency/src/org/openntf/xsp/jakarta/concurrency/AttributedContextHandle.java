package org.openntf.xsp.jakarta.concurrency;

import java.util.HashMap;
import java.util.Map;

import org.glassfish.enterprise.concurrent.spi.ContextHandle;

/**
 * This {@link ContextHandle} instance provides access to arbitrary
 * contextual attributes.
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public class AttributedContextHandle implements ContextHandle {
	private static final long serialVersionUID = 1L;
	
	// TODO figure out what to do about Serialization
	private final Map<String, Object> attributes;
	
	public AttributedContextHandle() {
		this.attributes = new HashMap<>();
	}
	
	public void setAttribute(String attrName, Object value) {
		this.attributes.put(attrName, value);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getAttribute(String attrName) {
		return (T)this.attributes.get(attrName);
	}
}