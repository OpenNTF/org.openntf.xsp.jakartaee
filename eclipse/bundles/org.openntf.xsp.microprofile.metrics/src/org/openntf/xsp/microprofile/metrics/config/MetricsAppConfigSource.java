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
package org.openntf.xsp.microprofile.metrics.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.microprofile.config.ext.ImplicitAppConfigProvider;
import org.openntf.xsp.microprofile.metrics.MetricsResourceContributor;

/**
 * This MP Config provider produces the current app's context path as a prefix
 * for metrics, to allow for app-specific metric collection.
 *
 * @author Jesse Gallagher
 * @since 2.10.0
 */
public class MetricsAppConfigSource implements ImplicitAppConfigProvider {
	public static final String CONFIG_APPNAME = "mp.metrics.appName"; //$NON-NLS-1$
	public static final String TAG_APP = "_app"; //$NON-NLS-1$

	@Override
	public Map<String, String> get() {
		if(!"false".equals(LibraryUtil.getApplicationProperty(MetricsResourceContributor.PROP_ENABLED, "true"))) { //$NON-NLS-1$ //$NON-NLS-2$
			Map<String, String> result = new HashMap<>();
			ComponentModuleLocator.getDefault()
				.flatMap(ComponentModuleLocator::getServletContext)
				.ifPresent(ctx -> {
					result.put(CONFIG_APPNAME, '/' + ctx.getContextPath());
				});
			return result;
		} else {
			return Collections.emptyMap();
		}
	}

}
