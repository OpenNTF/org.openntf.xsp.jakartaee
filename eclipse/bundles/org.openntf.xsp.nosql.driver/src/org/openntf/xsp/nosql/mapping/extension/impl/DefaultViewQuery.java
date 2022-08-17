package org.openntf.xsp.nosql.mapping.extension.impl;

import org.openntf.xsp.nosql.mapping.extension.ViewQuery;

/**
 * Default implementation of {@link ViewQuery}.
 * 
 * @author Jesse Gallagher
 * @since 2.8.0
 */
public class DefaultViewQuery implements ViewQuery {
	private String category;
	private Object viewKey;
	private boolean exact = false;

	@Override
	public ViewQuery category(String category) {
		this.category = category;
		return this;
	}
	
	@Override
	public String getCategory() {
		return category;
	}

	@Override
	public ViewQuery key(Object key, boolean exact) {
		this.viewKey = key;
		this.exact = exact;
		return this;
	}
	
	@Override
	public Object getKey() {
		return viewKey;
	}
	
	@Override
	public boolean isExact() {
		return exact;
	}

}
