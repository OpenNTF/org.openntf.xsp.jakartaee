package org.openntf.xsp.jaxrs;

import java.util.Collection;

import jakarta.ws.rs.core.Application;

/**
 * Extension interface to contribute additional resource classes
 * at {@link Application} init.
 * 
 * @author Jesse Gallagher
 * @since 2.1.0
 */
public interface JAXRSClassContributor {
	Collection<Class<?>> getClasses();
}
