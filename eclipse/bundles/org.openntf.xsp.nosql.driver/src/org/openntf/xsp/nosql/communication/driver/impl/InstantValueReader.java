package org.openntf.xsp.nosql.communication.driver.impl;

import java.time.Instant;
import java.time.temporal.TemporalAccessor;
import java.util.Date;

import jakarta.nosql.ValueReader;

public class InstantValueReader implements ValueReader {

	@Override
	public boolean test(Class<?> t) {
		return Instant.class.equals(t);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T read(Class<T> clazz, Object value) {
		if(value == null) {
			return null;
		} else if(value instanceof TemporalAccessor) {
			return (T)Instant.from((TemporalAccessor)value);
		} else if(value instanceof String) {
			if(((String)value).isEmpty()) {
				return null;
			} else {
				return (T)Instant.parse((String)value);
			}
		} else if(value instanceof Date) {
			return (T)((Date)value).toInstant();
		}
		throw new UnsupportedOperationException("Unable to convert value of type " + value.getClass().getName() + " to Instant");
	}

}
