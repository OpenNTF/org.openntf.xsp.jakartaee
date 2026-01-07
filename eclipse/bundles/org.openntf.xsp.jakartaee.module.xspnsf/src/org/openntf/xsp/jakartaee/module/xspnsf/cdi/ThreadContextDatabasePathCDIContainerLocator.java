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
package org.openntf.xsp.jakartaee.module.xspnsf.cdi;

import java.util.Optional;

import javax.servlet.ServletException;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;
import com.ibm.domino.xsp.module.nsf.NSFService;

import org.openntf.xsp.jakarta.cdi.ext.CDIContainerLocator;
import org.openntf.xsp.jakarta.cdi.util.ContainerUtil;

import jakarta.annotation.Priority;

/**
 * This {@link CDIContainerLocator} looks for a thread-context database path,
 * which may be specified as an override by user applications.
 *
 * @author Jesse Gallagher
 * @since 2.8.0
 */
@Priority(100)
public class ThreadContextDatabasePathCDIContainerLocator implements CDIContainerLocator {
	@Override
	public Object getContainer() {
		String nsfPath = ContainerUtil.getThreadContextDatabasePath();
		if(StringUtil.isNotEmpty(nsfPath)) {
			LCDEnvironment lcd = LCDEnvironment.getInstance();
			NSFService nsfService = lcd.getServices().stream()
				.filter(NSFService.class::isInstance)
				.map(NSFService.class::cast)
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("Unable to locate active NSFService"));
			try {
				return Optional.ofNullable(nsfService.loadModule(nsfPath));
			} catch(ServletException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}
}
