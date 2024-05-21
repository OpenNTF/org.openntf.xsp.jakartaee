/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.nosql.communication.driver.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Optional;
import java.util.Vector;

import org.eclipse.jnosql.mapping.metadata.EntityMetadata;

/**
 * Contains methods common among multiple driver implementations when converting
 * entities.
 * 
 * @author Jesse Gallagher
 * @since 2.8.0
 */
public abstract class AbstractEntityConverter {
	
	protected <T extends Annotation> Optional<T> getFieldAnnotation(EntityMetadata classMapping, String fieldName, Class<T> annotation) {
		if(classMapping == null) {
			return Optional.empty();
		}
		return classMapping.fields()
			.stream()
			.filter(field -> fieldName.equals(field.name()))
			.findFirst()
			.map(EntityUtil::getNativeField)
			.map(field -> field.getAnnotation(annotation));
	}
	
	protected Optional<Type> getFieldType(EntityMetadata classMapping, String fieldName) {
		if(classMapping == null) {
			return Optional.empty();
		}
		return classMapping.fields()
			.stream()
			.filter(field -> fieldName.equals(field.name()))
			.findFirst()
			.map(EntityUtil::getNativeField)
			.map(field -> field.getGenericType());
	}
	
	protected String composeEtag(String universalId, Temporal modTime) {
		Instant inst = Instant.from(modTime);
		return md5(universalId + inst.getEpochSecond() + inst.getNano());
	}
	
	public static String md5(String value) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
			md.update(String.valueOf(value).getBytes());
			byte[] digest = md.digest();
			StringBuilder sb = new StringBuilder(digest.length * 2);
			for (byte b : digest) {
				sb.append(String.format("%02x", b)); //$NON-NLS-1$
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}
	
	public static Object applyPrecision(Object dominoVal, int precision) {
		if(dominoVal instanceof Number) {
			BigDecimal decimal = BigDecimal.valueOf(((Number)dominoVal).doubleValue());
			return decimal.setScale(precision, RoundingMode.HALF_UP).doubleValue();
		} else if(dominoVal instanceof Collection && !((Collection<?>)dominoVal).isEmpty()) {
			Vector<Object> result = new Vector<>(((Collection<?>)dominoVal).size());
			for(Object obj : ((Collection<?>)dominoVal)) {
				if(obj instanceof Number) {
					BigDecimal decimal = BigDecimal.valueOf(((Number)obj).doubleValue());
					result.add(decimal.setScale(precision, RoundingMode.HALF_UP).doubleValue());
				} else {
					result.add(obj);
				}
			}
			return result;
		} else {
			return dominoVal;
		}
	}
}
