/**
 * Copyright © 2018-2021 Martin Pradny and Jesse Gallagher
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
package org.openntf.xsp.microprofile.openapi.config;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigValue;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.eclipse.microprofile.config.spi.Converter;

public class NOPConfig implements Config {

	@Override
	public <T> T getValue(String propertyName, Class<T> propertyType) {
		throw new NoSuchElementException();
	}

	@Override
	public ConfigValue getConfigValue(String propertyName) {
		return NOPConfigValue.instance;
	}

	@Override
	public <T> Optional<T> getOptionalValue(String propertyName, Class<T> propertyType) {
		return Optional.empty();
	}

	@Override
	public Iterable<String> getPropertyNames() {
		return Collections.emptySet();
	}

	@Override
	public Iterable<ConfigSource> getConfigSources() {
		return Collections.emptySet();
	}

	@Override
	public <T> Optional<Converter<T>> getConverter(Class<T> forType) {
		return Optional.empty();
	}

	@Override
	public <T> T unwrap(Class<T> type) {
		throw new IllegalArgumentException("Unwrapping not supported in NOPConfig");
	}

}
