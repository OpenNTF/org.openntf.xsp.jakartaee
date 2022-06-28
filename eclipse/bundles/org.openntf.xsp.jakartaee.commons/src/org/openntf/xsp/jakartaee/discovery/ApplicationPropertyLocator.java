package org.openntf.xsp.jakartaee.discovery;

/**
 * This extension class allows contributions to the process of determining
 * the value of a given application property.
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public interface ApplicationPropertyLocator {
	/**
	 * @return {@code true} if the locator is in a context where it can run
	 */
	boolean isActive();
	
	/**
	 * @param prop the property to retrieve
	 * @param defaultValue the default value to return when the property is not set
	 * @return {@link prop} if set in the application; {@code defaultValue} otherwise
	 */
	String getApplicationProperty(String prop, String defaultValue);
}
