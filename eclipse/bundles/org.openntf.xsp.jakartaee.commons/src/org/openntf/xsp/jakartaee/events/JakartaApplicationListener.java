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
	default void applicationCreated(final ApplicationEx application) {}

	default void applicationDestroyed(final ApplicationEx application) {}

	default void applicationRefreshed(final ApplicationEx application) {}
}
