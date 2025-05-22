package org.openntf.xsp.jakartaee.module;

import java.util.Collection;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;

import jakarta.servlet.ServletContainerInitializer;

/**
 * Allows contribution of {@link ServletContainerInitializer} for compatible
 * modules.
 * 
 * @since 3.4.0
 */
public interface ServletContainerInitializerProvider {
	Collection<ServletContainerInitializer> provide(ComponentModule module);
}
