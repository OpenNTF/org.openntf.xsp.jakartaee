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
package org.openntf.xsp.cdi.provider;

import org.openntf.xsp.cdi.ext.CDIContainerLocator;
import org.openntf.xsp.cdi.ext.CDIContainerUtility;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import jakarta.annotation.Priority;

/**
 * This {@link CDIContainerLocator} looks for a thread-context database path,
 * which may be specified as an overried by user applications.
 * 
 * @author Jesse Gallagher
 * @since 2.8.0
 */
@Priority(3)
public class ThreadContextDatabasePathCDIContainerLocator implements CDIContainerLocator {

	@Override
	public String getNsfPath() {
		CDIContainerUtility util = LibraryUtil.findRequiredExtension(CDIContainerUtility.class);
		return util.getThreadContextDatabasePath();
	}

}
