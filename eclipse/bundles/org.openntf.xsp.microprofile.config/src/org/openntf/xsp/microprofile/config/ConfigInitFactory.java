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
package org.openntf.xsp.microprofile.config;

import java.security.AccessController;
import java.security.PrivilegedAction;

import com.ibm.designer.runtime.domino.adapter.HttpService;
import com.ibm.designer.runtime.domino.adapter.IServiceFactory;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;

import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;
import org.openntf.xsp.microprofile.config.sources.ImplicitAppConfigSourceFactory;
import org.openntf.xsp.microprofile.config.sources.NotesEnvironmentConfigSource;
import org.openntf.xsp.microprofile.config.sources.XspPropertiesConfigSourceFactory;

import io.smallrye.config.PropertiesLocationConfigSourceFactory;
import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.config.SmallRyeConfigProviderResolver;
import io.smallrye.config.SysPropConfigSource;
import jakarta.servlet.ServletContext;

public class ConfigInitFactory implements IServiceFactory {

	@Override
	public HttpService[] getServices(final LCDEnvironment env) {
		SmallRyeConfigProviderResolver resolver = new SmallRyeConfigProviderResolver() {
			@Override
			public SmallRyeConfigBuilder getBuilder() {
				SmallRyeConfigBuilder builder = super.getBuilder();

				// Manually add sources that would come from ServiceLoader
				builder.withSources(
					new SysPropConfigSource(),
					new NotesEnvironmentConfigSource()
				);
				builder.withSources(
					new PropertiesLocationConfigSourceFactory(),
					new XspPropertiesConfigSourceFactory(),
					new ImplicitAppConfigSourceFactory()
				);

				AccessController.doPrivileged((PrivilegedAction<Void>)() -> {
					ComponentModuleLocator.getDefault()
						.flatMap(ComponentModuleLocator::getServletContext)
						.map(ServletContext::getClassLoader)
						.ifPresent(builder::forClassLoader);
					return null;
				});

				return builder;
			}
		};

		ConfigProviderResolver.setInstance(resolver);

		return null;
	}

}
