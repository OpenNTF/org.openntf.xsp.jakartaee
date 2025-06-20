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
package org.openntf.xsp.jakarta.el;

import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.factory.FactoryLookup;

import org.openntf.xsp.jakarta.el.impl.RecordPropertyResolverFactory;
import org.openntf.xsp.jakartaee.events.JakartaApplicationListener;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import jakarta.annotation.Priority;

@Priority(2)
public class ELApplicationListener implements JakartaApplicationListener {
	public static final String PROP_PREFIX =  "org.openntf.xsp.jakarta.el.prefix"; //$NON-NLS-1$

	@Override
	public void applicationCreated(final ApplicationEx application) {
		if(LibraryUtil.usesLibrary(LibraryUtil.LIBRARY_CORE, application)) {
			@SuppressWarnings("deprecation")
			FactoryLookup facts = application.getFactoryLookup();

			String prefix = application.getProperty(PROP_PREFIX, ELBindingFactory.PREFIX);
			facts.setFactory(prefix, new ELBindingFactory(prefix));

			// Create a binding factory for default bindings
			facts.setFactory(XSPELBindingFactory.IBM_PREFIX, new XSPELBindingFactory(XSPELBindingFactory.IBM_PREFIX));

			facts.setFactory(RecordPropertyResolverFactory.class.getName(), RecordPropertyResolverFactory.INSTANCE);
		}
	}

	@Override
	public void applicationDestroyed(final ApplicationEx application) {

	}

	@Override
	public void applicationRefreshed(final ApplicationEx application) {
		applicationCreated(application);
	}

}
