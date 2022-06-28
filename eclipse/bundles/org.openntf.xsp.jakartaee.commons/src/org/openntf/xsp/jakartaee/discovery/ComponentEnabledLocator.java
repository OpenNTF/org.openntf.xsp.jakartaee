package org.openntf.xsp.jakartaee.discovery;

/**
 * This extension class allows contributions to the process of determining
 * whether a given XPages Jakarta EE component is enabled.
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public interface ComponentEnabledLocator {
	/**
	 * @return {@code true} if the locator is in a context where it can run
	 */
	boolean isActive();
	
	/**
	 * @param componentId the ID of the component to query
	 * @return {@code true} if the given component is enabled
	 */
	boolean isComponentEnabled(String componentId);
}
