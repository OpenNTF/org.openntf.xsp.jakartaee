package org.openntf.xsp.jakartaee.events;

import com.ibm.xsp.application.ApplicationEx;

/**
 * This service interface can be used to create XPages application listeners
 * that are executed in priority order via {@link jakarta.annotation.Priority}.
 * 
 * <p>Listeners are called in descending numerical order in
 * {@link #applicationCreated} and {@link #applicationRefreshed} and in
 * ascending numerical order in {@link #applicationDestroyed}. 
 */
public interface JakartaApplicationListener {
	default void applicationCreated(ApplicationEx application) {}
	
	default void applicationDestroyed(ApplicationEx application) {}
	
	default void applicationRefreshed(ApplicationEx application) {}
}
