package org.openntf.xsp.jakarta.nosql.communication.driver.impl;

import org.eclipse.jnosql.communication.ValueReader;

import jakarta.annotation.Priority;

@Priority(1)
public class IntValueReader implements ValueReader {

	@Override
	public boolean test(final Class<?> t) {
		return Integer.class.equals(t) || int.class.equals(t);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T read(final Class<T> type, final Object value) {
		if(value instanceof Number v) {
			return (T)Integer.valueOf(v.intValue());
		} else if(int.class.equals(type)) {
			return (T)Integer.valueOf(0);
		} else {
			return null;
		}
	}

}
