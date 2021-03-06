/**
 * Copyright © 2018-2020 Jesse Gallagher
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
package org.openntf.xsp.jsp;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.openntf.xsp.jsp.webapp.JspExtensionFactory;

import com.ibm.designer.runtime.domino.adapter.HttpService;
import com.ibm.designer.runtime.domino.adapter.IServiceFactory;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;
import com.ibm.domino.xsp.module.nsf.NSFService;
import com.ibm.ws.webcontainer.WebContainer;
import com.ibm.wsspi.webcontainer.logging.LoggerFactory;

/**
 * This {@link IServiceFactory} doesn't provide any HTTP services, but is used to
 * enable hooks very early in the HTTP init process.
 * 
 * @author Jesse Gallagher
 * @since 1.2.0
 */
public class EarlyInitFactory implements IServiceFactory {
	public static boolean debug = true;

	@Override
	public HttpService[] getServices(LCDEnvironment env) {
		try {
			initWebContainer();
		} catch(Throwable t) {
			t.printStackTrace();
		}
		try {
			initNsf();
		} catch(Throwable t) {
			t.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Adds JSP support to bundle-based web applications.
	 */
	private void initWebContainer() {
		if(debug) {
			Logger logger = LoggerFactory.getInstance().getLogger("com.ibm.ws.webcontainer.servlet"); //$NON-NLS-1$
			logger.setLevel(Level.ALL);
		}
		WebContainer.addExtensionFactory(new JspExtensionFactory());
	}
	
	/**
	 * Adds JSP support to NSFs.
	 */
	private void initNsf() {
		// Register ".jsp" with the NSF service, which will then pass along to JspServletFactory
		NSFService.addHandledExtensions(".jsp"); //$NON-NLS-1$
	}

}
