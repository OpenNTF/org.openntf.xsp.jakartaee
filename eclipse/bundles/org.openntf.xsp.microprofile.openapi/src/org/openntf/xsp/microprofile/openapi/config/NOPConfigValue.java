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

import org.eclipse.microprofile.config.ConfigValue;

public class NOPConfigValue implements ConfigValue {
	public static final NOPConfigValue instance = new NOPConfigValue();

	@Override
	public String getName() {
		return null;
	}

	@Override
	public String getValue() {
		return null;
	}

	@Override
	public String getRawValue() {
		return null;
	}

	@Override
	public String getSourceName() {
		return null;
	}

	@Override
	public int getSourceOrdinal() {
		return 0;
	}

}
