/**
 * Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakarta.rest.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.xsp.application.ApplicationEx;

import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.bound.BoundLiteral;
import org.jboss.weld.context.bound.BoundRequestContext;
import org.openntf.xsp.jakarta.cdi.ext.CDIConstants;
import org.openntf.xsp.jakarta.cdi.util.ContainerUtil;
import org.openntf.xsp.jakarta.rest.ServiceParticipant;
import org.openntf.xsp.jakartaee.AbstractXspLifecycleServlet;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * An {@link ServletContainer} subclass that provides a Faces context to the
 * servlet request.
 *
 * @author Martin Pradny
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class JakartaRestServlet extends AbstractXspLifecycleServlet {
	private static final long serialVersionUID = 1L;

	public static final String KEY_CDI_STORAGE = JakartaRestServlet.class.getName() + "_cdistorage"; //$NON-NLS-1$

	private final HttpServletDispatcher delegate;

	public JakartaRestServlet(final ComponentModule module) {
		super(module);
		this.delegate = new HttpServletDispatcher();
	}

	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		request.setAttribute(CDIConstants.CDI_JAXRS_REQUEST, "true"); //$NON-NLS-1$
		super.service(request, response);
	}

	@Override
	protected void doInit(final ServletConfig config, final HttpServletRequest request) throws ServletException {
		initCdi(request);
		delegate.init(config);
	}

	@Override
	protected void doService(final HttpServletRequest request, final HttpServletResponse response, final ApplicationEx application) throws ServletException, IOException {
		initCdi(request);
		List<ServiceParticipant> participants = LibraryUtil.findExtensions(ServiceParticipant.class, getModule());
		for(ServiceParticipant participant : participants) {
    		participant.doBeforeService(request, response);
    	}
    	ServletUtil.getListeners(request.getServletContext(), ServletRequestListener.class)
			.forEach(l -> l.requestInitialized(new ServletRequestEvent(getServletContext(), request)));

    	try {
        	delegate.service(request, response);
		} finally {
    		ServletUtil.getListeners(request.getServletContext(), ServletRequestListener.class)
				.forEach(l -> l.requestDestroyed(new ServletRequestEvent(getServletContext(), request)));
    		for(ServiceParticipant participant : participants) {
	    		participant.doAfterService(request, response);
	    	}
    		termCdi(request);

			// In case it's not flushed on its own
			ServletUtil.close(response);
    	}
	}

	@Override
	public void destroy() {
		super.destroy();
		delegate.destroy();
	}

	private void initCdi(final HttpServletRequest request) {
		if(request != null && request.getAttribute(KEY_CDI_STORAGE) == null) {
			CDI<Object> cdi = ContainerUtil.getContainer(this.getModule());
			BoundRequestContext context = (BoundRequestContext)cdi.select(RequestContext.class, BoundLiteral.INSTANCE).get();
			Map<String, Object> cdiScope = new HashMap<>();
			request.setAttribute(KEY_CDI_STORAGE, cdiScope);
			context.associate(cdiScope);
			context.activate();
		}
	}
	@SuppressWarnings("unchecked")
	private void termCdi(final HttpServletRequest request) {
		if(request != null) {
			CDI<Object> cdi = ContainerUtil.getContainer(this.getModule());
			BoundRequestContext context = (BoundRequestContext)cdi.select(RequestContext.class, BoundLiteral.INSTANCE).get();
			context.invalidate();
			context.deactivate();
			context.dissociate((Map<String, Object>)request.getAttribute(KEY_CDI_STORAGE));
		}
	}
}
