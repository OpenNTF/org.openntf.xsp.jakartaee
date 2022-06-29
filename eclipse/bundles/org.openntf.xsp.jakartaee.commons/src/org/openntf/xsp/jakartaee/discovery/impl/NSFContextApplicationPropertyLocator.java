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

import org.openntf.xsp.jakartaee.discovery.ApplicationPropertyLocator;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.domino.xsp.module.nsf.NotesContext;

import jakarta.annotation.Priority;

/**
 * Determines whether a given component is enabled based on its ID being
 * present in the enabled XPages Libraries in the current {@link NotesContent}
 * {@link NotesDatabase}.
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
@Priority(1)
public class NSFContextApplicationPropertyLocator implements ApplicationPropertyLocator {

	@Override
	public boolean isActive() {
		NotesContext ctx = NotesContext.getCurrentUnchecked();
		if(ctx != null) {
			try {
				NotesDatabase database = ctx.getNotesDatabase();
				if(database != null) {
					return true;
				}
			} catch(NotesAPIException e) {
				// Ignore
			}
		}
		return false;
	}

	@Override
	public String getApplicationProperty(String prop, String defaultValue) {
		NotesContext ctx = NotesContext.getCurrentUnchecked();
		try {
			NotesDatabase database = ctx.getNotesDatabase();
			if(database != null) {
				return LibraryUtil.getXspProperties(database).getProperty(prop, defaultValue);
			}
		} catch(NotesAPIException e) {
			throw new RuntimeException(e);
		}
		return defaultValue;
	}

}
