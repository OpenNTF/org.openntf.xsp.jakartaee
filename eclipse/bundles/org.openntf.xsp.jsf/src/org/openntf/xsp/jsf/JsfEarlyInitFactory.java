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
package org.openntf.xsp.jsf;

import com.ibm.designer.runtime.domino.adapter.HttpService;
import com.ibm.designer.runtime.domino.adapter.IServiceFactory;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;
import com.ibm.domino.xsp.module.nsf.NSFService;


/**
 * This {@link IServiceFactory} doesn't provide any HTTP services, but is used to
 * enable hooks very early in the HTTP init process.
 * 
 * @author Jesse Gallagher
 * @since 2.4.0
 */
public class JsfEarlyInitFactory implements IServiceFactory {
	public static boolean debug = true;

	@Override
	public HttpService[] getServices(LCDEnvironment env) {
		try {
			initNsf();
		} catch(Throwable t) {
			t.printStackTrace();
		}
		
		
		return null;
	}
	
	/**
	 * Adds JSP support to NSFs.
	 */
	private void initNsf() {
		// Register ".xhtml" with the NSF service, which will then pass along to JsfServletFactory
		NSFService.addHandledExtensions(".xhtml"); //$NON-NLS-1$
		NSFService.addHandledExtensions(".jsf"); //$NON-NLS-1$
	}
}
