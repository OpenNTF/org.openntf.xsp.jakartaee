package org.openntf.xsp.nosql.mapping.extension;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation can be placed on a method in a {@link DominoRepository}
 * interface to indicate that the results should be pulled
 * from the named view, retrieving the document for each applicable entry.
 * 
 * @author Jesse Gallagher
 * @since 2.6.0
 * 
 * @see {@link ViewCategory}
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface ViewDocuments {
	/**
	 * @return the name of the view to read
	 */
	String value();
	
	/**
	 * @return the maximum entry level to process
	 */
	int maxLevel() default -1;
}
