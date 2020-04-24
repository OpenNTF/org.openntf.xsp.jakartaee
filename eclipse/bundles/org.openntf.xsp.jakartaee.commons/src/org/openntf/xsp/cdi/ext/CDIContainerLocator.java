package org.openntf.xsp.cdi.ext;

/**
 * This IBM Commons extension interface represents an object capable of locating
 * a CDI container for a given class in Domino when normal mechanisms fail.
 * 
 * @author Jesse Gallagher
 * @since 1.2.0
 */
public interface CDIContainerLocator {
	static final String EXTENSION_POINT = CDIContainerLocator.class.getName();
	
	/**
	 * Indicates that the CDI container should be loaded based on the configuration
	 * of the provided NSF.
	 * 
	 * @return an API path to an NSF, or {@code null} to skip providing this information
	 */
	String getNsfPath();
	
	/**
	 * Indicates that rhe CDI container should be loaded based on a given OSGi bundle.
	 * 
	 * @return a symbolic name of a bundle, or {@code null} to skip providing this information
	 */
	String getBundleId();
}
