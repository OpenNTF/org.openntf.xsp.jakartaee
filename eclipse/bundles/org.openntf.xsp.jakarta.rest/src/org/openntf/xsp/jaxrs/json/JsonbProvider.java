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
package org.openntf.xsp.jaxrs.json;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

/**
 * Provides the {@link Jsonb} instance produced by {@link Jsonb}
 * to the JAX-RS environment.
 * 
 * @author Jesse Gallagher
 * @since 2.11.0
 */
@Provider
public class JsonbProvider implements ContextResolver<Jsonb> {

	@Override
	public Jsonb getContext(Class<?> type) {
		if(Jsonb.class.equals(type)) {
			Instance<Jsonb> bean = CDI.current().select(Jsonb.class);
			if(bean.isResolvable()) {
				return bean.get();
			} else {
				return JsonbBuilder.create();
			}
		}
		return null;
	}

}
