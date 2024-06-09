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
package org.openntf.xsp.jakarta.rest.json;

import java.util.Collection;
import java.util.Collections;

import org.openntf.xsp.jakarta.rest.RestClassContributor;

import jakarta.json.bind.Jsonb;

/**
 * Contributes a provider that will emit the {@link Jsonb}
 * instance from CDI for JAX-RS.
 * 
 * @author Jesse Gallagher
 * @since 2.11.0
 */
public class JsonbRestContributor implements RestClassContributor {

	@Override
	public Collection<Class<?>> getClasses() {
		return Collections.singleton(JsonbProvider.class);
	}

}
