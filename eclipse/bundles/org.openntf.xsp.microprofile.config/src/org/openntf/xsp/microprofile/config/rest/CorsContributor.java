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
package org.openntf.xsp.microprofile.config.rest;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.resteasy.plugins.interceptors.CorsFilter;
import org.openntf.xsp.jakarta.rest.RestClassContributor;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

public class CorsContributor implements RestClassContributor {

	public static final String PROP_CORS_ENABLE = "rest.cors.enable"; //$NON-NLS-1$
	public static final String PROP_CORS_CREDENTIALS = "rest.cors.allowCredentials"; //$NON-NLS-1$
	public static final String PROP_CORS_METHODS = "rest.cors.allowedMethods"; //$NON-NLS-1$
	public static final String PROP_CORS_ALLOWEDHEADERS = "rest.cors.allowedHeaders"; //$NON-NLS-1$
	public static final String PROP_CORS_EXPOSEDHEADERS = "rest.cors.exposedHeaders"; //$NON-NLS-1$
	public static final String PROP_CORS_MAXAGE = "rest.cors.maxAge"; //$NON-NLS-1$
	public static final String PROP_CORS_ORIGINS = "rest.cors.allowedOrigins"; //$NON-NLS-1$

	@Override
	public Collection<Class<?>> getClasses() {
		return Collections.emptySet();
	}

	@Override
	public Collection<Object> getSingletons() {
		if(LibraryUtil.isLibraryActive(LibraryUtil.LIBRARY_MICROPROFILE)) {
			Config config = ConfigProvider.getConfig();

			Optional<Boolean> enabled = config.getOptionalValue(PROP_CORS_ENABLE, boolean.class);
			if(enabled.isPresent() && enabled.get()) {
				CorsFilter filter = new CorsFilter();

				boolean credentials = config.getOptionalValue(PROP_CORS_CREDENTIALS, boolean.class)
					.orElse(true);
				filter.setAllowCredentials(credentials);
				String allowedMethods = config.getOptionalValue(PROP_CORS_METHODS, String.class)
					.orElse(null);
				filter.setAllowedMethods(allowedMethods);
				String allowedHeaders = config.getOptionalValue(PROP_CORS_ALLOWEDHEADERS, String.class)
					.orElse(null);
				filter.setAllowedHeaders(allowedHeaders);
				String exposedHeaders = config.getOptionalValue(PROP_CORS_EXPOSEDHEADERS, String.class)
					.orElse(null);
				filter.setExposedHeaders(exposedHeaders);
				int maxAge = config.getOptionalValue(PROP_CORS_MAXAGE, int.class)
					.orElse(-1);
				filter.setCorsMaxAge(maxAge);

				List<String> allowedOrigins = config.getOptionalValues(PROP_CORS_ORIGINS, String.class)
					.orElseGet(() -> Arrays.asList("*")); //$NON-NLS-1$
				filter.getAllowedOrigins().addAll(allowedOrigins);

				return Collections.singleton(filter);
			}
		}
		return Collections.emptySet();
	}

}
