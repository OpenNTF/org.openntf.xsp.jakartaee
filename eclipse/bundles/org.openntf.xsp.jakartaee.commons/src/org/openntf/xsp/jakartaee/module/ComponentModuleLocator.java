package org.openntf.xsp.jakartaee.module;

import java.util.Optional;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This extension interface can be used to define a service that
 * can attempt to locate the active {@link ComponentModule} and
 * {@link ServletContext} instances.
 * 
 * @author Jesse Gallagher
 * @since 2.8.0
 */
public interface ComponentModuleLocator<T extends ComponentModule> {
	/**
	 * Attempts to find the active module, returning an empty
	 * value when this locator does not apply.
	 * 
	 * @return an {@link Optional} describing the current
	 *         {@link ComponentModule} instance, or an empty one
	 *         if this locator does not apply
	 */
	Optional<T> findActiveModule();
	
	/**
	 * Attempts to find the active {@link ServletContext}, returning
	 * an empty value when this locator does not apply or when the
	 * active module does not supply a Servlet context.
	 * 
	 * @return an {@link Optional} describing the current
	 *         {@link ServletContext}, or an empty one if this
	 *         locator does not apply
	 */
	Optional<ServletContext> findServletContext();
	
	/**
	 * Attempts to find the active {@link HttpServletRequest}, returning
	 * an empty value when this locator does not apply or when the
	 * active module does not supply a Servlet request.
	 * 
	 * @return an {@link Optional} describing the current
	 *         {@link HttpServletRequest}, or an empty one if this
	 *         locator does not apply
	 */
	Optional<HttpServletRequest> findServletRequest();
}
