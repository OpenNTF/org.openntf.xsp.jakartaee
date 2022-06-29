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
package org.openntf.xsp.jakartaee.discovery.impl;

import org.openntf.xsp.jakartaee.discovery.ComponentEnabledLocator;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.domino.osgi.core.context.ContextInfo;

/**
 * Determines whether a given component is enabled based on its ID being
 * present in the enabled XPages Libraries in the context NSF of an OSGi-based
 * servlet request.
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public class OSGiServletComponentEnabledLocator implements ComponentEnabledLocator {

	@Override
	public boolean isActive() {
		try {
			NotesDatabase database = ContextInfo.getServerDatabase();
			if(database != null) {
				return true;
			}
		} catch(NoClassDefFoundError e) {
			// Ignore
		} catch (NotesAPIException e) {
			throw new RuntimeException(e);
		}
		return false;
	}

	@Override
	public boolean isComponentEnabled(String componentId) {
		try {
			NotesDatabase database = ContextInfo.getServerDatabase();
			if(database != null) {
				return LibraryUtil.usesLibrary(componentId, database);
			}
		} catch(NoClassDefFoundError e) {
			// Ignore
		} catch (NotesAPIException e) {
			throw new RuntimeException(e);
		}
		return true;
	}

}
