package org.openntf.xsp.nosql.communication.driver.impl;

import java.lang.annotation.Annotation;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.Optional;

import org.eclipse.jnosql.mapping.reflection.ClassMapping;
import org.eclipse.jnosql.mapping.reflection.FieldMapping;

/**
 * Contains methods common among multiple driver implementations when converting
 * entities.
 * 
 * @author Jesse Gallagher
 * @since 2.8.0
 */
public abstract class AbstractEntityConverter {
	protected <T extends Annotation> Optional<T> getFieldAnnotation(ClassMapping classMapping, String fieldName, Class<T> annotation) {
		if(classMapping == null) {
			return Optional.empty();
		}
		return classMapping.getFields()
			.stream()
			.filter(field -> fieldName.equals(field.getName()))
			.findFirst()
			.map(FieldMapping::getNativeField)
			.map(field -> field.getAnnotation(annotation));
	}
	
	protected Optional<Class<?>> getFieldType(ClassMapping classMapping, String fieldName) {
		if(classMapping == null) {
			return Optional.empty();
		}
		return classMapping.getFields()
			.stream()
			.filter(field -> fieldName.equals(field.getName()))
			.findFirst()
			.map(FieldMapping::getNativeField)
			.map(field -> field.getType());
	}
	
	protected String composeEtag(String universalId, Temporal modTime) {
		Instant inst = Instant.from(modTime);
		return md5(universalId + inst.getEpochSecond() + inst.getNano());
	}
	
	protected String md5(String value) {
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
}
