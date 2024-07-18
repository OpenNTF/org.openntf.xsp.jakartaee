package org.openntf.xsp.jakarta.nosql.communication.driver.impl;

import java.util.Collections;

import org.eclipse.jnosql.communication.ValueReader;

public class IterableValueReader implements ValueReader {

	@Override
	public boolean test(Class<?> t) {
		return Iterable.class.isAssignableFrom(t);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T read(Class<T> type, Object value) {
		if(value instanceof Iterable i) {
			return (T)i;
		}
		return (T)Collections.singleton(value);
	}

}
