/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
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

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openntf.xsp.cdi.ext.CDIContainerLocator;
import org.openntf.xsp.cdi.ext.CDIContainerUtility;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.jakartaee.util.ModuleUtil;
import org.osgi.framework.Bundle;

import com.ibm.commons.util.StringUtil;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.CDIProvider;

/**
 * Provides access to the current application's CDI context.
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class DominoCDIProvider implements CDIProvider {
	private static final Logger log = Logger.getLogger(DominoCDIProvider.class.getPackage().getName());
	
	@SuppressWarnings("unchecked")
	@Override
	public synchronized CDI<Object> getCDI() {
		CDIContainerUtility util = LibraryUtil.findRequiredExtension(CDIContainerUtility.class);
		
		// Check in any available locator extensions
		List<CDIContainerLocator> locators = LibraryUtil.findExtensionsSorted(CDIContainerLocator.class, false);
		try {
			for(CDIContainerLocator locator : locators) {
				Object container = locator.getContainer();
				if(container != null) {
					return (CDI<Object>)container;
				}
				
				String nsfPath = locator.getNsfPath();
				if(StringUtil.isNotEmpty(nsfPath)) {
					container = ModuleUtil.getNSFComponentModule(nsfPath)
						.map(util::getContainer)
						.orElse(null);
					if(container != null) {
						return (CDI<Object>)container;
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
		} catch(IllegalStateException e) {
			// Will almost definitely be "Invalid disposed application ClassLoader", which occurs
			//   during active development of an NSF - ignore
			// https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/362
		} catch(Exception e) {
			if(log.isLoggable(Level.SEVERE)) {
				log.log(Level.SEVERE, "Encountered exception trying to load CDI container", e);
			}
			throw e;
		}
		
		return null;
	}
	
	@Override
	public int getPriority() {
		return DEFAULT_CDI_PROVIDER_PRIORITY+2;
	}

}
