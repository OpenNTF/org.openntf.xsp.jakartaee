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
package org.openntf.xsp.microprofile.health;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.openntf.xsp.cdi.discovery.WeldBeanClassContributor;
import org.openntf.xsp.cdi.util.DiscoveryUtil;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;

import io.smallrye.health.ResponseProvider;
import jakarta.enterprise.inject.spi.Extension;

/**
 * @author Jesse Gallagher
 * @since 2.2.0
 */
public class HealthBeanContributor implements WeldBeanClassContributor {

	@Override
	public Collection<Class<?>> getBeanClasses() {
		if(LibraryUtil.isLibraryActive(HealthLibrary.LIBRARY_ID)) {
			// Look for annotated beans in io.smallrye.health
			Bundle bundle = FrameworkUtil.getBundle(ResponseProvider.class);
			try {
				return DiscoveryUtil.findExportedClassNames(bundle, false)
					.map(t -> {
						try {
							return bundle.loadClass(t);
						} catch (ClassNotFoundException e) {
							throw new RuntimeException(e);
						}
					})
					// TODO filter to only annotated
					.collect(Collectors.toList());
			} catch (BundleException e) {
				throw new RuntimeException(e);
			}
		} else {
			return null;
		}
	}

	@Override
	public Collection<Extension> getExtensions() {
		return Collections.emptyList();
	}

}
