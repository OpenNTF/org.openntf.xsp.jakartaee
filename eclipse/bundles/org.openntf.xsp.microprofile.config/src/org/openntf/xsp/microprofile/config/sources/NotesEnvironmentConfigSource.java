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

import java.util.Collections;
import java.util.Set;

import com.ibm.domino.napi.NException;
import com.ibm.domino.napi.c.Os;

import org.eclipse.microprofile.config.spi.ConfigSource;

/**
 * {@link ConfigSource} implementation that reads properties from the
 * active Notes environment's notes.ini values.
 *
 * @author Jesse Gallagher
 * @since 2.2.0
 */
public class NotesEnvironmentConfigSource implements ConfigSource {

	@Override
	public String getName() {
		return "NotesEnvironment"; //$NON-NLS-1$
	}

	@Override
	public Set<String> getPropertyNames() {
		return Collections.emptySet();
	}

	@Override
	public String getValue(final String propertyName) {
		try {
			return Os.OSGetEnvironmentString(propertyName);
		} catch (NException e) {
			throw new RuntimeException(e);
		}
	}

}
