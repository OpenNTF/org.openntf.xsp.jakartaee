/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.microprofile.config.ext;

import java.util.Map;
import java.util.function.Supplier;

import jakarta.annotation.Priority;

/**
 * This extension class allows for contributors to provide configuration
 * properties dynamically for an application.
 *
 * <p>These providers can be annotated with {@link Priority} to control
 * the order in which they are applied. Providers with higher priority
 * values will be applied after those with lower values.</p>
 *
 * @author Jesse Gallagher
 * @since 2.10.0
 */
public interface ImplicitAppConfigProvider extends Supplier<Map<String, String>> {
}
