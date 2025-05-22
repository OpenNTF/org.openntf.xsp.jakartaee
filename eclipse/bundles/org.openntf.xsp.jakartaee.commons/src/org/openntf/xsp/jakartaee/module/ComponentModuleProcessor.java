/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.xsp.jakartaee.module;

import java.util.Optional;
import java.util.stream.Stream;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;

import jakarta.servlet.ServletConfig;

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
	
	default boolean emulateServletEvents(final T module) {
		return true;
	}
	
	default Optional<javax.servlet.Servlet> initXPagesServlet(final T module, final ServletConfig servletConfig) {
		return Optional.empty();
	}
	
	default void initializeSessionAsSigner(final T module) {
		// NOP
	}
	
	default boolean usesBundleClassLoader(final T module) {
		return false;
	}
	
	default boolean hasImplicitCdi(final T module) {
		return false;
	}
}
