package org.openntf.xsp.jakarta.nosql.communication.driver.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.StreamSupport;

import org.eclipse.jnosql.communication.ValueReader;

public class ListValueReader implements ValueReader {

	@Override
	public boolean test(Class<?> t) {
		return List.class.isAssignableFrom(t);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T read(Class<T> type, Object value) {
		if(value instanceof List i) {
			return (T)i;
		} else if(value instanceof Iterable i) {
			return (T)StreamSupport.stream(i.spliterator(), false).toList();
		}
		return (T)Collections.singletonList(value);
	}

}
