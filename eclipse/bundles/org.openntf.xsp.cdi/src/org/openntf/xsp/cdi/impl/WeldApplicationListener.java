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
package org.openntf.xsp.cdi.impl;

import org.jboss.weld.environment.se.WeldContainer;
import org.openntf.xsp.cdi.CDILibrary;
import org.openntf.xsp.cdi.util.ContainerUtil;
import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.application.events.ApplicationListener2;

/**
 * Manages the lifecycle of the app's associated Weld instance.
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class WeldApplicationListener implements ApplicationListener2 {
	
	@Override
	public void applicationCreated(ApplicationEx application) {
		// NOP
	}

	@Override
	public void applicationDestroyed(ApplicationEx application) {
		if(LibraryUtil.usesLibrary(CDILibrary.LIBRARY_ID, application)) {
			ComponentModuleLocator.getDefault()
				.map(ComponentModuleLocator::getActiveModule)
				.map(ContainerUtil::getContainerUnchecked)
				.ifPresent(container -> {
					if(container instanceof WeldContainer) {
						WeldContainer c = (WeldContainer)container;
						if(c.isRunning()) {
							c.close();
						}
					}
				});
		}
	}

	@Override
	public void applicationRefreshed(ApplicationEx application) {
		// NOP
	}
}
