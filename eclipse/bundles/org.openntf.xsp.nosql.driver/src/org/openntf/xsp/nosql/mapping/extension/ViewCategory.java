package org.openntf.xsp.nosql.mapping.extension;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation can be added to a method parameter in a {@link DominoRepository}
 * interface to indicate that it should be used as a category to restrict view
 * navigation to.
 * 
 * @author Jesse Gallagher
 * @since 2.6.0
 */
@Documented
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface ViewCategory {
}
