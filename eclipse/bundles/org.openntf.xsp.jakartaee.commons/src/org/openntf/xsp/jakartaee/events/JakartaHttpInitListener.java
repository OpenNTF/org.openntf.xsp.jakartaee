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
