package it.org.openntf.xsp.jakartaee;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.Test;

import jakarta.json.JsonObject;

public class TempTest {
	Map<String, Object> someField;
	
	@Test
	public void runTest() throws NoSuchFieldException, SecurityException {
		Field field = TempTest.class.getDeclaredField("someField");
		Type mapType = field.getGenericType();
        if(!(mapType instanceof java.lang.reflect.ParameterizedType)) {
			java.lang.reflect.Type[] interfaces = ((java.lang.Class)mapType).getGenericInterfaces();
			for(int i = 0; i < interfaces.length; i++) {
				if(interfaces[i].getTypeName().startsWith("java.util.Map")) {
					mapType = interfaces[i];
					break;
				}
			}
        }
		Class<?> keyType = (Class<?>) ((ParameterizedType) mapType)
                .getActualTypeArguments()[0];
	}
}
