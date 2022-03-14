/**
 * Copyright © 2018-2022 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.nosql;

import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import jakarta.nosql.ServiceLoaderProvider;

/**
 * @author Jesse Gallagher
 * @since 2.3.0
 */
public class NoSQLActivator implements BundleActivator {

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		try {
			ServiceLoaderProvider.setLoader(c -> {
				return StreamSupport.stream(ServiceLoader.load(c, c.getClassLoader()).spliterator(), false);
			});
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
	}

}
