/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.xsp.jakarta.nosql.communication.driver.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jnosql.communication.ValueWriter;
import org.eclipse.jnosql.mapping.metadata.ClassInformationNotFoundException;
import org.eclipse.jnosql.mapping.metadata.EntitiesMetadata;
import org.eclipse.jnosql.mapping.metadata.EntityMetadata;
import org.eclipse.jnosql.mapping.metadata.FieldMetadata;
import org.eclipse.jnosql.mapping.reflection.DefaultFieldMetadata;
import org.openntf.xsp.jakarta.nosql.mapping.extension.DocumentConfig;

import jakarta.activation.MimetypesFileTypeMap;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.nosql.Column;

/**
 * Contains utility methods for working with NoSQL entities.
 *
 * @author Jesse Gallagher
 * @since 2.9.0
 */
@SuppressWarnings({ "removal", "deprecation" })
public enum EntityUtil {
	;
	
	private static final ThreadLocal<MessageDigest> MD5 = ThreadLocal.withInitial(() -> {
		try {
			return MessageDigest.getInstance("MD5"); //$NON-NLS-1$
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("Unable to load MD5 provider", e);
		}
	});
	
	private static final MimetypesFileTypeMap MIME_TYPES_MAP = new MimetypesFileTypeMap();

	// For now, assume that all implementations used AbstractFieldMetadata
	private static final Field fieldField;
	
	private static final Method osgiLookupMethod;
	
	static {
		fieldField = AccessController.doPrivileged((PrivilegedAction<Field>)() -> {
			try {
				Field result = DefaultFieldMetadata.class.getSuperclass().getDeclaredField("field"); //$NON-NLS-1$
				result.setAccessible(true);
				return result;
			} catch (NoSuchFieldException | SecurityException e) {
				throw new RuntimeException(e);
			}
		});
		
		osgiLookupMethod = AccessController.doPrivileged((PrivilegedAction<Method>)() -> {
			try {
				try {
					Class<?> osgiServiceLoader = Class.forName("org.glassfish.hk2.osgiresourcelocator.ServiceLoader", true, EntityUtil.class.getClassLoader()); //$NON-NLS-1$
					return osgiServiceLoader.getMethod("lookupProviderInstances", Class.class); //$NON-NLS-1$
				} catch(ClassNotFoundException e) {
					return null;
				}
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	public static EntityMetadata getClassMapping(final String entityName) {
		EntitiesMetadata mappings = CDI.current().select(EntitiesMetadata.class).get();
		try {
			return mappings.findByName(entityName);
		} catch(ClassInformationNotFoundException e) {
			// Shouldn't happen, but we should account for it
			return null;
		}
	}

	public static Map<String, Class<?>> getItemTypes(final EntityMetadata classMapping) {
		return classMapping == null ? Collections.emptyMap() : classMapping.fields()
			.stream()
			.collect(Collectors.toMap(
				(Function<? super FieldMetadata, ? extends String>) FieldMetadata::name,
				f -> getNativeField(f).getType()
			));
	}

	/**
	 * Determines the back-end item name for the given Java property.
	 *
	 * @param propName the Java property to check
	 * @param mapping the {@link ClassMapping} instance for the class in question
	 * @return the effective item name based on the class properties
	 */
	public static String findItemName(final String propName, final EntityMetadata mapping) {
		if(mapping != null) {
			Column annotation = mapping.fieldMapping(propName)
				.map(EntityUtil::getNativeField)
				.map(f -> f.getAnnotation(Column.class))
				.orElse(null);
			if(annotation != null && !annotation.value().isEmpty()) {
				return annotation.value();
			} else {
				return propName;
			}
		} else {
			return propName;
		}
	}

	public static Field getNativeField(final FieldMetadata fieldMapping) {
		try {
			return (Field)fieldField.get(fieldMapping);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getFormName(final EntityMetadata classMapping) {
		DocumentConfig ann = classMapping.type().getAnnotation(DocumentConfig.class);
		if(ann != null) {
			return ann.formName();
		} else {
			return classMapping.name();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static List<ValueWriter<Object, Object>> getValueWriters() {
		if(osgiLookupMethod == null) {
			// Use the usual ServiceLoader
			return ServiceLoader.load(ValueWriter.class).stream()
				.map(ServiceLoader.Provider::get)
				.map(vw -> (ValueWriter<Object, Object>)vw)
				.toList();
		} else {
			try {
				return (List<ValueWriter<Object, Object>>)osgiLookupMethod.invoke(null, ValueWriter.class);
			} catch (IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public static String composeEtag(final String universalId, final long modTime) {
		BigInteger etag = new BigInteger(universalId, 16);
		BigInteger mod = BigInteger.valueOf(modTime);
		etag = etag.xor(mod);
		etag = etag.xor(mod.shiftLeft(64));
		String val = etag.toString(16);
		int len = val.length();
		if(len < 32) {
			return "0".repeat(32-len) + val; //$NON-NLS-1$
		} else {
			return val;
		}
	}

	public static String md5(final String value) {
		MessageDigest md = MD5.get();
		md.update(String.valueOf(value).getBytes());
		byte[] digest = md.digest();
		StringBuilder sb = new StringBuilder(digest.length * 2);
		for (byte b : digest) {
			String hex = Integer.toHexString(b & 0xFF);
			if(hex.length() == 1) {
				sb.append('0');
			}
			sb.append(hex);
		}
		return sb.toString();
	}
	
	public static String guessContentType(final String fileName) {
		String contentType = URLConnection.guessContentTypeFromName(fileName);
		if(contentType != null && !contentType.isEmpty()) {
			return contentType;
		}

	    contentType = MIME_TYPES_MAP.getContentType(fileName);
		if(contentType != null && !contentType.isEmpty()) {
			return contentType;
		}

		return "application/octet-stream"; //$NON-NLS-1$
	}
}
