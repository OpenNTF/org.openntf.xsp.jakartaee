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
package org.openntf.xsp.jakarta.cdi.impl;

import com.ibm.xsp.application.ApplicationEx;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.jboss.weld.environment.se.WeldContainer;
import org.openntf.xsp.jakarta.cdi.util.ContainerUtil;
import org.openntf.xsp.jakartaee.events.JakartaApplicationListener;
import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import jakarta.annotation.Priority;

/**
 * Manages the lifecycle of the app's associated Weld instance.
 *
 * @author Jesse Gallagher
 * @since 1.0.0
 */
@Priority(0)
public class CDIApplicationListener implements JakartaApplicationListener {
	private static final Logger log = System.getLogger(CDIApplicationListener.class.getPackageName());

	@Override
	public void applicationDestroyed(final ApplicationEx application) {
		if(LibraryUtil.usesLibrary(LibraryUtil.LIBRARY_CORE, application)) {
			try {
				ComponentModuleLocator.getDefault()
					.map(ComponentModuleLocator::getActiveModule)
					.map(ContainerUtil::getContainerUnchecked)
					.ifPresent(container -> {
						if(container instanceof WeldContainer c) {
							if(c.isRunning()) {
								c.close();
							}
						}
					});
			} catch(Throwable t) {
				log.log(Level.ERROR, "Encountered exception shutting down CDI container", t);
			}
		}
	}
}
