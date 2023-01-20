package org.openntf.xsp.jakartaee.metrics;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation can be placed on a REST resource class to declare that
 * its methods should not be included in Metrics tracking
 * 
 * @author Jesse Gallagher
 * @since 2.10.0
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
public @interface MetricsIgnore {

}
