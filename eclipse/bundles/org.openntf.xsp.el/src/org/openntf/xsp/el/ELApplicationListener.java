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
package org.openntf.xsp.el;

import org.openntf.xsp.jakartaee.util.LibraryUtil;

import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.application.events.ApplicationListener2;
import com.ibm.xsp.factory.FactoryLookup;

public class ELApplicationListener implements ApplicationListener2 {

	@Override
	public void applicationCreated(ApplicationEx application) {
		if(LibraryUtil.usesLibrary(ELLibrary.LIBRARY_ID, application)) {
			@SuppressWarnings("deprecation")
			FactoryLookup facts = application.getFactoryLookup();
			
			String prefix = application.getProperty(ELLibrary.PROP_PREFIX, ELBindingFactory.PREFIX);
			facts.setFactory(prefix, new ELBindingFactory(prefix));
			
			// Create a binding factory for default bindings
			facts.setFactory(XSPELBindingFactory.IBM_PREFIX, new XSPELBindingFactory(XSPELBindingFactory.IBM_PREFIX));
		}
	}

	@Override
	public void applicationDestroyed(ApplicationEx application) {
		
	}

	@Override
	public void applicationRefreshed(ApplicationEx application) {
		applicationCreated(application);
	}

}
