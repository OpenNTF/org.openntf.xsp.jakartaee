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
package org.openntf.xsp.jsp.webapp;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.jasper.servlet.JspServlet;
import org.osgi.framework.Bundle;

import com.ibm.ws.jsp.Constants;
import com.ibm.ws.webcontainer.extension.WebExtensionProcessor;
import com.ibm.wsspi.webcontainer.servlet.IServletContext;

/**
 * {@link com.ibm.wsspi.webcontainer.extension.ExtensionProcessor ExtensionProcessor} implementation that
 * delegates web app JSP requests to the Equinox bundle JSP compiler.
 * 
 * @author Jesse Gallagher
 * @since 2.1.0
 */
public class JspExtensionProcessor extends WebExtensionProcessor {
	public class JspWebAppServletConfig implements ServletConfig {

		@Override
		public String getInitParameter(String param) {
			return null;
		}

		@Override
		public Enumeration<String> getInitParameterNames() {
			return Collections.emptyEnumeration();
		}

		@Override
		public ServletContext getServletContext() {
			return webApp;
		}

		@Override
		public String getServletName() {
			return JspExtensionProcessor.class.getSimpleName();
		}
		
	}
	
	private final IServletContext webApp;
	private final Bundle bundle;
	//private final JspServlet delegate;
	
	public JspExtensionProcessor(IServletContext webApp) {
		super(webApp);
		this.webApp = webApp;
		
		// This package isn't exported, so get to it reflectively.
		// We know it's a com.ibm.pvc.internal.webcontainer.webapp.BundleWebApp
		this.bundle = AccessController.doPrivileged((PrivilegedAction<Bundle>)() -> {
			try {
				Method getBundle = webApp.getClass().getMethod("getBundle"); //$NON-NLS-1$
				return (Bundle)getBundle.invoke(webApp);
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		});
		// TODO look into reading contentLocation from plugin.xml
//		this.delegate = new JspServlet(bundle, "WebContent", null); //$NON-NLS-1$
//		try {
//			this.delegate.init(new JspWebAppServletConfig());
//		} catch (Throwable e) {
//			e.printStackTrace();
//		}
	}

	@Override
	@SuppressWarnings("nls")
	public void handleRequest(ServletRequest request, ServletResponse response) throws Exception {
		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse resp = (HttpServletResponse)response;
		
		
		try {
//			this.delegate.service(request, response);
		} catch(Throwable t) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, MessageFormat.format("Exception processing {0}", req.getServletPath()));
			t.printStackTrace();
		}
	}

	@Override
	public List<String> getPatternList() {
		return Arrays.asList(Constants.STANDARD_JSP_EXTENSIONS);
	}

}
