package org.openntf.xsp.jakarta.nosql.mapping.extension;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation can be used on an entity class to indicate the
 * actual form name to use for stored documents.
 * 
 * <p>This can be useful to distinguish the entity name from the form
 * name in the case that distinct entities from different databases have
 * the same form name.</p>
 * 
 * @since 3.4.0
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface FormName {
	/**
	 * @return the value to use for the Form item in documents
	 */
	String value();
}
