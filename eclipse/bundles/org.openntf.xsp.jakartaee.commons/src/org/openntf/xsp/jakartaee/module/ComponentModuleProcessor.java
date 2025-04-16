package org.openntf.xsp.jakartaee.module;

import java.util.stream.Stream;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;

/**
 * This service interface represents a processor able to handle common operations
 * on a {@link ComponentModule} instance of a given type.
 *
 * @since 3.4.0
 */
public interface ComponentModuleProcessor<T extends ComponentModule> {
	boolean canProcess(ComponentModule module);

	Stream<String> getClassNames(T module);

	Stream<String> listFiles(T module, String basePath);

	default String getModuleId(final T module) {
		return Integer.toHexString(System.identityHashCode(module));
	}
	
	default String getXspPrefix(final T module) {
		return ""; //$NON-NLS-1$
	}
	
	default boolean isJakartaModule(final T module) {
		return false;
	}
	
	default boolean hasXPages(final T module) {
		return false;
	}
}
