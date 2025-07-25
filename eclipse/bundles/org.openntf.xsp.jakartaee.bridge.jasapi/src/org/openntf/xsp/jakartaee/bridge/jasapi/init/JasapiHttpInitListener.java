/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakartaee.bridge.jasapi.init;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.domino.bridge.http.jasapi.JavaSapiEnvironment;
import com.ibm.domino.bridge.http.jasapi.JavaSapiService;

import org.openntf.xsp.jakartaee.bridge.jasapi.JasapiServiceFactory;
import org.openntf.xsp.jakartaee.events.JakartaHttpInitListener;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

public class JasapiHttpInitListener implements JakartaHttpInitListener {
	private static final Logger log = Logger.getLogger(JasapiHttpInitListener.class.getPackage().getName());
	
	@Override
	public void httpInit() throws Exception {
		AccessController.doPrivileged((PrivilegedAction<Void>)() -> {
			try {
				JavaSapiEnvironment env = findEnvironment();
				env.registerServices();

				List<JasapiServiceFactory> extensions = LibraryUtil.findExtensions(JasapiServiceFactory.class);
				if(!extensions.isEmpty()) {
					Field servicesField = JavaSapiEnvironment.class.getDeclaredField("services"); //$NON-NLS-1$
					servicesField.setAccessible(true);

					JavaSapiService[] existing = (JavaSapiService[])servicesField.get(env);
					List<JavaSapiService> services = new ArrayList<>(Arrays.asList(existing));
					extensions.stream()
						.map(fac -> fac.getServices(env))
						.filter(Objects::nonNull)
						.flatMap(Collection::stream)
						.filter(Objects::nonNull)
						.forEach(services::add);
					servicesField.set(env, services.toArray(new JavaSapiService[services.size()]));
				}

			} catch(Throwable t) {
				if(log.isLoggable(Level.SEVERE)) {
					log.log(Level.SEVERE, "Encountered exception initializing JavaSapi services", t);
				}
			}

			return null;
		});
	}

	private JavaSapiEnvironment findEnvironment() {
		JavaSapiEnvironment env = JavaSapiEnvironment.getInstance();
		if(env == null) {
			env = new JavaSapiEnvironment();
		}
		return env;
	}
}
