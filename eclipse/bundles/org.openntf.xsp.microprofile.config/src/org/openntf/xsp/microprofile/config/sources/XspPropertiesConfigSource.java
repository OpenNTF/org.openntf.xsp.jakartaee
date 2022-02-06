/**
 * Copyright © 2018-2022 Jesse Gallagher
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

import org.eclipse.microprofile.config.spi.ConfigSource;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

/**
 * {@link ConfigSource} implementation that reads properties from
 * the current NSF's xsp.properties file.
 * 
 * @author Jesse Gallagher
 * @since 2.2.0
 */
public class XspPropertiesConfigSource implements ConfigSource {
	public XspPropertiesConfigSource() {
	}
	
	@Override
	public String getName() {
		return "XSPProperties"; //$NON-NLS-1$
	}
	
	@Override
	public Set<String> getPropertyNames() {
		return Collections.emptySet();
	}

	@Override
	public String getValue(String propertyName) {
		return LibraryUtil.getApplicationProperty(propertyName, null);
	}

}
