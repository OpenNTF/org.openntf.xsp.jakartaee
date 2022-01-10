package org.openntf.xsp.jaxrs.security;

/**
 * This exception is used to signal to providers that the user is not
 * allowed to access the provided resource.
 * 
 * @author Jesse Gallagher
 * @since 2.2.0
 */
public class NotAuthorizedSignal extends RuntimeException {
	private static final long serialVersionUID = 1L;

}
