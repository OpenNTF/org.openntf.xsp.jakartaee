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
package org.openntf.xsp.microprofile.metrics;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.openntf.xsp.cdi.CDILibrary;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.jaxrs.JAXRSClassContributor;
import org.openntf.xsp.microprofile.metrics.jaxrs.MetricsResource;
import org.openntf.xsp.microprofile.metrics.jaxrs.RestMetricsFilter;

public class MetricsResourceContributor implements JAXRSClassContributor {
	public static final String PROP_ENABLED = "rest.mpmetrics.enable"; //$NON-NLS-1$

	@Override
	public Collection<Class<?>> getClasses() {
		if(LibraryUtil.isLibraryActive(CDILibrary.LIBRARY_ID)) {
			if(!"false".equals(LibraryUtil.getApplicationProperty(PROP_ENABLED, "true"))) { //$NON-NLS-1$ //$NON-NLS-2$
				return Arrays.asList(
					RestMetricsFilter.class,
					MetricsResource.class
				);
			} else {
				return Collections.emptySet();
			}
		} else {
			return Collections.emptySet();
		}
	}

}
