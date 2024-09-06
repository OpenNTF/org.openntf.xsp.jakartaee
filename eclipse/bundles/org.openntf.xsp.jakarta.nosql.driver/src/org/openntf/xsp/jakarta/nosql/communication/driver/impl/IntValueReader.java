package org.openntf.xsp.jakarta.nosql.communication.driver.impl;

import jakarta.annotation.Priority;
import org.eclipse.jnosql.communication.ValueReader;

@Priority(1)
public class IntValueReader implements ValueReader {

	@Override
	public boolean test(Class<?> t) {
		return Integer.class.equals(t) || int.class.equals(t);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T read(Class<T> type, Object value) {
		if(value instanceof Number v) {
			return (T)Integer.valueOf(v.intValue());
		} else if(int.class.equals(type)) {
			return (T)Integer.valueOf(0);
		} else {
			return (T)null;
		}
	}

}
