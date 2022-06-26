package org.openntf.xsp.nosql.mapping.extension;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.nosql.mapping.Column;

/**
 * This can be placed on a field annotated with {@link Column @Column} to
 * provide hints to the driver about the flags to apply to the item when
 * stored in a Domino document.
 * 
 * @author Jesse Gallagher
 * @since 2.6.0
 */
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface ItemFlags {
	boolean authors() default false;
	boolean readers() default false;
	boolean names() default false;
	boolean encrypted() default false;
	boolean signed() default false;
	boolean protectedItem() default false;
	boolean summary() default true;
	/**
	 * Indicates that the item should not actually be written to storage
	 * when the backend document is saved.
	 * @since 2.7.0
	 */
	boolean saveToDisk() default true;
}
