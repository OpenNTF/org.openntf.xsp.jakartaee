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
package org.openntf.xsp.microprofile.config.sources;

import java.util.Collections;

import org.eclipse.microprofile.config.spi.ConfigSource;

import io.smallrye.config.ConfigSourceContext;
import io.smallrye.config.ConfigSourceFactory;

/**
 * @author Jesse Gallagher
 * @since 2.10.0
 */
public class ImplicitAppConfigSourceFactory implements ConfigSourceFactory {

	@Override
	public Iterable<ConfigSource> getConfigSources(final ConfigSourceContext context) {
		return Collections.singleton(new ImplicitAppConfigSource());
	}

}
