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
package org.openntf.xsp.jakarta.faces;

import com.ibm.domino.xsp.module.nsf.NSFService;

import org.openntf.xsp.jakartaee.events.JakartaHttpInitListener;

/**
 * @since 3.4.0
 */
public class FacesHttpInitListener implements JakartaHttpInitListener {
	@Override
	public void httpInit() throws Exception {
		// Register ".xhtml" with the NSF service, which will then pass along to FacesServletFactory
		NSFService.addHandledExtensions(".xhtml"); //$NON-NLS-1$
		NSFService.addHandledExtensions(".jsf"); //$NON-NLS-1$
	}
}
