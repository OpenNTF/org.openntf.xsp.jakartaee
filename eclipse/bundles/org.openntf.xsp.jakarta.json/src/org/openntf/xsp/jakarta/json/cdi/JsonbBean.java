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
package org.openntf.xsp.jakarta.json.cdi;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.spi.JsonProvider;

/**
 * Provides a {@link Jsonb} instance optionally configured with a
 * {@link JsonbConfig} object produced by the application.
 *
 * @author Jesse Gallagher
 * @since 2.11.0
 */
@ApplicationScoped
public class JsonbBean {
	private Jsonb jsonb;
	
	@PostConstruct
	public void buildJsonb() {
		JsonbBuilder builder = JsonbBuilder.newBuilder();

		Instance<JsonbConfig> configBean = CDI.current().select(JsonbConfig.class);
		if(configBean.isResolvable()) {
			builder = builder.withConfig(configBean.get());
		}

		Instance<JsonProvider> providerBean = CDI.current().select(JsonProvider.class);
		if(providerBean.isResolvable()) {
			builder = builder.withProvider(providerBean.get());
		}

		this.jsonb = builder.build();
	}
	
	@Produces
	public Jsonb getJsonb() {
		return this.jsonb;
	}
}
