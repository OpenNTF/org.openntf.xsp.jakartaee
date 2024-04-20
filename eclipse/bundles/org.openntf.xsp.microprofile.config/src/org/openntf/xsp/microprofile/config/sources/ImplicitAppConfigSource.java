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
package org.openntf.xsp.microprofile.config.sources;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.microprofile.config.ext.ImplicitAppConfigProvider;

/**
 * @author Jesse Gallagher
 * @since 2.10.0
 */
public class ImplicitAppConfigSource implements ConfigSource {
	
	private final Map<String, String> properties = new HashMap<>();
	
	public ImplicitAppConfigSource() {
		LibraryUtil.findExtensionsSorted(ImplicitAppConfigProvider.class, true)
			.stream()
			.map(ImplicitAppConfigProvider::get)
			.filter(Objects::nonNull)
			.forEach(properties::putAll);
	}

	@Override
	public Set<String> getPropertyNames() {
		return properties.keySet();
	}

	@Override
	public String getValue(String propertyName) {
		return properties.get(propertyName);
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}
	
	@Override
	public int getOrdinal() {
		return DEFAULT_ORDINAL-1;
	}

}
