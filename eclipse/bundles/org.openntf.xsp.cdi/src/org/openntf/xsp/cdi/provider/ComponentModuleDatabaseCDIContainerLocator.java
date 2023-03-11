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
package org.openntf.xsp.cdi.provider;

import java.io.IOException;

import org.openntf.xsp.cdi.ext.CDIContainerLocator;
import org.openntf.xsp.cdi.ext.CDIContainerUtility;
import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;

import jakarta.annotation.Priority;

/**
 * This {@link CDIContainerLocator} looks for a contextual database
 * in an active {@link ComponentModule} for a CDI container.
 * 
 * @author Jesse Gallagher
 * @since 2.10.0
 */
@Priority(1)
public class ComponentModuleDatabaseCDIContainerLocator implements CDIContainerLocator {

	@Override
	public Object getContainer() {
		CDIContainerUtility util = LibraryUtil.findRequiredExtension(CDIContainerUtility.class);
		
		return ComponentModuleLocator.getDefault()
			.flatMap(ComponentModuleLocator::getNotesDatabase)
			.map(t -> {
				try {
					return util.getContainer(t);
				} catch (NotesAPIException | IOException e) {
					return null;
				}
			})
			.orElse(null);
	}

}
