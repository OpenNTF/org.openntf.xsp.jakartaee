package org.openntf.xsp.jakartaee.module.jakartansf;

import java.util.Collection;
import java.util.EventListener;
import java.util.Set;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;

import org.openntf.xsp.jakartaee.module.ServletContainerInitializerProvider;
import org.openntf.xsp.jakartaee.module.jakartansf.concurrency.NSFJakartaModuleConcurrencyListener;

import jakarta.servlet.ServletContainerInitializer;

/**
 * @since 3.5.0
 */
public class NSFJakartaModuleListenerProvider implements ServletContainerInitializerProvider {

	@Override
	public Collection<ServletContainerInitializer> provide(ComponentModule module) {
		return null;
	}
	
	@Override
	public Collection<Class<? extends EventListener>> provideListeners(ComponentModule module) {
		return Set.of(
			NSFJakartaModuleConcurrencyListener.class
		);
	}

}
