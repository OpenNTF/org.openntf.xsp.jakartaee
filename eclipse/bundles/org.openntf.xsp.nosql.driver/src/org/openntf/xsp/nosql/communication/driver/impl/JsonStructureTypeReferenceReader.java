package org.openntf.xsp.nosql.communication.driver.impl;

import java.util.List;

import org.eclipse.jnosql.communication.TypeReferenceReader;
import org.eclipse.jnosql.communication.TypeSupplier;

import jakarta.json.JsonArray;
import jakarta.json.JsonStructure;

/**
 * @since 2.14.0
 */
public class JsonStructureTypeReferenceReader implements TypeReferenceReader {

	@Override
	public boolean test(TypeSupplier<?> t) {
		if(t.get() instanceof Class) {
			return JsonStructure.class.isAssignableFrom((Class<?>)t.get());
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T convert(TypeSupplier<T> typeReference, Object value) {
		if(value instanceof JsonArray) {
			return (T)value;
		}
		if(value instanceof List && ((List<?>)value).size() == 1) {
			value = ((List<?>)value).get(0);
		}
		return (T)value;
	}

}
