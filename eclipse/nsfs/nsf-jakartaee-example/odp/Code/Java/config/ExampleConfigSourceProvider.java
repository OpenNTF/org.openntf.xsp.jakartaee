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
package config;

import java.util.Collections;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSourceProvider;

/**
 * Demonstrates the use of a custom {@link ConfigSourceProvider} implementation registered
 * in {@code /META-INF/services/org.eclipse.microprofile.config.spi.ConfigSourceProvider}
 * 
 * @since 2.11.0
 */
public class ExampleConfigSourceProvider implements ConfigSourceProvider {
	private static final Iterable<ConfigSource> SOURCES = Collections.singleton(new ExampleProvidedConfigSource());

	@Override
	public Iterable<ConfigSource> getConfigSources(ClassLoader forClassLoader) {
		return SOURCES;
	}

}
