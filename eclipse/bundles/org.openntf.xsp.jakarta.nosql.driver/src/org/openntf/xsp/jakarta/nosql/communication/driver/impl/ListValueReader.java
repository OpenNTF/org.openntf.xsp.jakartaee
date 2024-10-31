package org.openntf.xsp.jakarta.nosql.communication.driver.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.StreamSupport;

import org.eclipse.jnosql.communication.ValueReader;

public class ListValueReader implements ValueReader {

	@Override
	public boolean test(final Class<?> t) {
		return List.class.isAssignableFrom(t);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T read(final Class<T> type, final Object value) {
		if(value instanceof List i) {
			return (T)i;
		} else if(value instanceof Iterable i) {
			return (T)StreamSupport.stream(i.spliterator(), false).toList();
		}
		return (T)Collections.singletonList(value);
	}

}
