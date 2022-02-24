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
package org.openntf.xsp.cdi.provider;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.CDIProvider;

import org.openntf.xsp.cdi.ext.CDIContainerLocator;
import org.openntf.xsp.cdi.ext.CDIContainerUtility;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.osgi.framework.Bundle;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.designer.domino.napi.NotesSession;
import com.ibm.domino.osgi.core.context.ContextInfo;
import com.ibm.xsp.application.ApplicationEx;

/**
 * Provides access to the current application's Weld context.
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class NSFCDIProvider implements CDIProvider {
	@SuppressWarnings("unchecked")
	@Override
	public synchronized CDI<Object> getCDI() {
		CDI<Object> result = null;
		
		CDIContainerUtility util = LibraryUtil.findRequiredExtension(CDIContainerUtility.class);
		
		String databasePath = util.getThreadContextDatabasePath();
		if(StringUtil.isNotEmpty(databasePath)) {
			try {
				NotesSession session = new NotesSession();
				try {
					NotesDatabase database = session.getDatabase(databasePath);
					if(database != null) {
						database.open();
						try {
							result = (CDI<Object>)util.getContainer(database);
						} finally {
							database.recycle();
						}
					}
				} finally {
					session.recycle();
				}
			} catch(Throwable t) {
				t.printStackTrace();
			}
		}
		if(result != null) {
			return result;
		}
			
		
		ApplicationEx application = ApplicationEx.getInstance();
		if(application != null) {
			result = (CDI<Object>)util.getContainer(application);
		}
		if(result != null) {
			return result;
		}
		
		try {
			NotesDatabase database = ContextInfo.getServerDatabase();
			if(database != null) {
				result = (CDI<Object>)util.getContainer(database);
			}
		} catch(Throwable t) {
			t.printStackTrace();
		}
		if(result != null) {
			return result;
		}
		
		// Check in any available locator extensions
		List<CDIContainerLocator> locators = LibraryUtil.findExtensions(CDIContainerLocator.class);
		NotesSession session = new NotesSession();
		try {
			for(CDIContainerLocator locator : locators) {
				String nsfPath = locator.getNsfPath();
				if(StringUtil.isNotEmpty(nsfPath)) {
					try {
						NotesDatabase database = session.getDatabaseByPath(nsfPath);
						try {
							database.open();
							result = (CDI<Object>)util.getContainer(database);
							if(result != null) {
								return result;
							}
						} finally {
							if(database != null) {
								database.recycle();
							}
						}
					} catch (NotesAPIException | IOException e) {
						// Log and move on
						e.printStackTrace();
					}
				}
				
				String bundleId = locator.getBundleId();
				if(StringUtil.isNotEmpty(bundleId)) {
					Optional<Bundle> bundle = LibraryUtil.getBundle(bundleId);
					if(bundle.isPresent()) {
						return (CDI<Object>)util.getContainer(bundle.get());
					}
				}
			}
		} finally {
			try {
				session.recycle();
			} catch (NotesAPIException e) {
				// Ignore
			}
		}
		
		return null;
	}
	
	@Override
	public int getPriority() {
		return DEFAULT_CDI_PROVIDER_PRIORITY+2;
	}

}
