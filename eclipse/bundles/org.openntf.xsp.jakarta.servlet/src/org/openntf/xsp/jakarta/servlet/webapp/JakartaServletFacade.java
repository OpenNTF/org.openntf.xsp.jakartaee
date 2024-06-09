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
package org.openntf.xsp.jakarta.servlet.webapp;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;

import org.openntf.xsp.jakarta.cdi.util.DiscoveryUtil;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;

import com.ibm.commons.util.StringUtil;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * General-purpose Servlet implementation meant to wrap a {@code jakarta.*}
 * Servlet for a {@link javax.*} environment.
 * 
 * <p>Users can specify the Jakarta Servlet class name either by specifying the
 * {@value #INIT_PARAM_SERVLETCLASS} init parameter or
 * by subclassing this and overriding {@link #getJakartaServletClassName()}.</p>
 *  
 * @author Jesse Gallagher
 * @since 2.8.0
 */
public class JakartaServletFacade extends javax.servlet.http.HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public static final String INIT_PARAM_SERVLETCLASS = "org.openntf.xsp.jakarta.servlet.class"; //$NON-NLS-1$
	
	private Servlet delegate;

	@Override
	public void init(javax.servlet.ServletConfig config) throws javax.servlet.ServletException {
		super.init(config);
		
		String className = getJakartaServletClassName();
		if(StringUtil.isEmpty(className)) {
			throw new IllegalArgumentException(MessageFormat.format("Servlet class name must be specified via the {0} init parameter or getJakartaServletClassName()", INIT_PARAM_SERVLETCLASS));
		}
		try {
			@SuppressWarnings("unchecked")
			Class<? extends Servlet> delegateClass = (Class<? extends Servlet>) Class.forName(className, true, Thread.currentThread().getContextClassLoader());
			if(Arrays.stream(delegateClass.getAnnotations()).anyMatch(DiscoveryUtil::isBeanDefining) || delegateClass.isAnnotationPresent(WebServlet.class)) {
				this.delegate = CDI.current().select(delegateClass).get();
			} else {
				this.delegate = delegateClass.getConstructor().newInstance();
			}
		} catch (Exception e) {
			throw new javax.servlet.ServletException(MessageFormat.format("Encountered exception loading Servlet of class {0}", className), e);
		}
		
		try {
			this.delegate.init(ServletUtil.oldToNew(config));
		} catch(ServletException e) {
			throw ServletUtil.newToOld(e);
		}
	}
	
	@Override
	protected void service(javax.servlet.http.HttpServletRequest oldReq, javax.servlet.http.HttpServletResponse oldResp)
			throws javax.servlet.ServletException, IOException {
		try {
			HttpServletRequest req = ServletUtil.oldToNew(getServletContext(), oldReq);
			HttpServletResponse resp = ServletUtil.oldToNew(oldResp);
			this.delegate.service(req, resp);
		} catch(ServletException e) {
			throw ServletUtil.newToOld(e);
		}
	}
	
	@Override
	public void destroy() {
		delegate.destroy();
		
		super.destroy();
	}
	
	public String getJakartaServletClassName() {
		return getServletConfig().getInitParameter(INIT_PARAM_SERVLETCLASS);
	}
	
	public Servlet getDelegate() {
		return delegate;
	}

}
