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
package org.openntf.xsp.jsp.webapp;

import java.util.Arrays;
import java.util.List;

import com.ibm.ws.jsp.Constants;
import com.ibm.wsspi.webcontainer.extension.ExtensionFactory;
import com.ibm.wsspi.webcontainer.extension.ExtensionProcessor;
import com.ibm.wsspi.webcontainer.servlet.IServletContext;

/**
 * {@link ExtensionFactory} implementation that enables JSP processing for bundle-based
 * web apps.
 * 
 * @author Jesse Gallagher
 * @since 2.1.0
 */
public class JspExtensionFactory implements ExtensionFactory {
	public static final String BUNDLE_WEBAPP_NAME = "com.ibm.pvc.internal.webcontainer.webapp.BundleWebApp"; //$NON-NLS-1$

	public JspExtensionFactory() {
	}

	@Override
	public ExtensionProcessor createExtensionProcessor(IServletContext context) throws Exception {
		if(context.getClass().getName().equals(BUNDLE_WEBAPP_NAME)) {
			return new JspExtensionProcessor(context);
		} else {
			return null;
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List getPatternList() {
		return Arrays.asList(Constants.STANDARD_JSP_EXTENSIONS);
	}

}
