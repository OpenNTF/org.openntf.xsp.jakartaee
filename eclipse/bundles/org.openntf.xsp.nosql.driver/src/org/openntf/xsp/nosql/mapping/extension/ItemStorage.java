package org.openntf.xsp.nosql.mapping.extension;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.nosql.mapping.Column;

/**
 * This can be placed on a field annotated with {@link Column @Column} to
 * provide hints to the driver about how to store the value of the item
 * property in the backend document.
 * 
 * @author Jesse Gallagher
 * @since 2.6.0
 */
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface ItemStorage {
	enum Type {
		/**
		 * Stores the value in the default format according to the data
		 * type.
		 */
		Default,
		/**
		 * Stores the value in a MIME part of type text/html.
		 */
		MIME,
		/**
		 * Stores the value as a "MIMEBean": a serialized Java object stored
		 * as a binary MIME part.
		 */
		MIMEBean,
		/**
		 * Stores the value as a JSON string serialized using JSON-B.
		 */
		JSON
	}
	
	Type type() default Type.Default;
	
	/**
	 * When storing an item using type {@link Type#MIME}, this value can be used to
	 * set the Content-Type header of the part.
	 * 
	 * @return the MIME type to use for MIME storage
	 */
	String mimeType() default "text/html";
	
	/**
	 * Indicates whether this value should be written to the back end when
	 * creating a new document.
	 * 
	 * @since 2.7.0
	 */
	boolean insertable() default true;
	/**
	 * Indicates whether this value should be written to the back end when
	 * updating an existing document.
	 * 
	 * @since 2.7.0
	 */
	boolean updatable() default true;
}
