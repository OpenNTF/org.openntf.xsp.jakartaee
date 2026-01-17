/**
 * Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.microprofile.config;

import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.openntf.xsp.jakartaee.events.JakartaHttpInitListener;

import io.smallrye.config.SmallRyeConfigProviderResolver;
import jakarta.annotation.Priority;

@Priority(1)
public class ConfigHttpInitListener implements JakartaHttpInitListener {
	@Override
	public void httpInit() throws Exception {
		SmallRyeConfigProviderResolver resolver = new JakartaConfigProviderResolver();

		ConfigProviderResolver.setInstance(resolver);
	}
}
