/**
 * Copyright (c) 2018-2023 Contributors to the XPages Jakarta EE Support Project
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
import org.openntf.xsp.microprofile.config.sources.NotesEnvironmentConfigSource;
import org.openntf.xsp.microprofile.config.sources.XspPropertiesConfigSourceFactory;

import com.ibm.designer.runtime.domino.adapter.HttpService;
import com.ibm.designer.runtime.domino.adapter.IServiceFactory;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;

import io.smallrye.config.PropertiesLocationConfigSourceFactory;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.config.SmallRyeConfigProviderResolver;
import io.smallrye.config.SysPropConfigSource;

public class ConfigInitFactory implements IServiceFactory {

	@Override
	public HttpService[] getServices(LCDEnvironment env) {
		SmallRyeConfigProviderResolver resolver = new SmallRyeConfigProviderResolver() {
			@Override
			public SmallRyeConfigBuilder getBuilder() {
				SmallRyeConfigBuilder builder = super.getBuilder();

				// Manually add sources that would come from ServiceLoader
				builder = builder.withSources(
					new SysPropConfigSource(),
					new NotesEnvironmentConfigSource()
				);
				builder = builder.withSources(
					new PropertiesLocationConfigSourceFactory(),
					new XspPropertiesConfigSourceFactory()
				);
				
				return builder;
			}
		};
		
		ConfigProviderResolver.setInstance(resolver);
		
		return null;
	}

}
