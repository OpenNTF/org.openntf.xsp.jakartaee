package org.openntf.xsp.nosql.mapping.extension.impl;

import org.openntf.xsp.nosql.mapping.extension.ViewQuery;
import org.openntf.xsp.nosql.mapping.extension.ViewQuery.ViewQueryProvider;

/**
 * Default implementation of {@link ViewQueryProvider}.
 * 
 * @author Jesse Gallagher
 * @since 2.8.0
 */
public class DefaultViewQueryProvider implements ViewQueryProvider {

	@Override
	public ViewQuery get() {
		return new DefaultViewQuery();
	}

}
