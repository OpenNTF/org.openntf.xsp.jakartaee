package org.openntf.xsp.jakartaee.events;

/**
 * This extension interface can be used to register a listener that will
 * be executed at HTTP start.
 * 
 * <p>Classes registered this way can be ordered using the
 * {@link jakarta.annotation.Priority @Priority} annotation, sorted in
 * descending order.</p>
 * 
 * <p>The intent of these listeners is to allow for extra processing at
 * HTTP init that may otherwise happen in an indeterminate order.</p>
 * 
 * @since 3.4.0
 */
public interface JakartaHttpInitListener {
	/**
	 * This method is called during HTTP initialization, specifically
	 * at the point when {@code IServiceFactory} instances are loaded.
	 */
	default void httpInit() throws Exception {
		
	}
	
	/**
	 * This method is called after all instances of this service have
	 * finished their {@link #httpInit()} methods.
	 */
	default void postInit() throws Exception {
		
	}
}
