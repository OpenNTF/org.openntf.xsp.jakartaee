package model;

import jakarta.nosql.ValueWriter;

public class CustomPropertyTypeWriter implements ValueWriter<CustomPropertyType, String> {

	@Override
	public boolean test(Class<?> c) {
		return CustomPropertyType.class.equals(c);
	}

	@Override
	public String write(CustomPropertyType object) {
		return object.getValue();
	}

}
