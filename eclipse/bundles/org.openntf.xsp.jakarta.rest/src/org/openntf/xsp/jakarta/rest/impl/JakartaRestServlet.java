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
package org.openntf.xsp.jakarta.rest.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.hibernate.validator.HibernateValidator;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.bound.BoundLiteral;
import org.jboss.weld.context.bound.BoundRequestContext;
import org.openntf.xsp.jakarta.cdi.ext.CDIConstants;
import org.openntf.xsp.jakarta.rest.ServiceParticipant;
import org.openntf.xsp.jakartaee.AbstractXspLifecycleServlet;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.xsp.application.ApplicationEx;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

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

	public JakartaRestServlet(ComponentModule module) {
		super(module);
		this.delegate = new HttpServletDispatcher();
	}
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setAttribute(CDIConstants.CDI_JAXRS_REQUEST, "true"); //$NON-NLS-1$
		initCdi(request);
		super.service(request, response);
	}
	
	@Override
	protected void doInit(ServletConfig config, HttpServletRequest request) throws ServletException {
		delegate.init(config);
	}
	
	@Override
	protected void doService(HttpServletRequest request, HttpServletResponse response, ApplicationEx application) throws ServletException, IOException {
		@SuppressWarnings("unchecked")
		List<ServiceParticipant> participants = (List<ServiceParticipant>)application.findServices(ServiceParticipant.EXTENSION_POINT);
		for(ServiceParticipant participant : participants) {
    		participant.doBeforeService(request, response);
    	}
    	ServletUtil.getListeners(request.getServletContext(), ServletRequestListener.class)
			.forEach(l -> l.requestInitialized(new ServletRequestEvent(getServletContext(), request)));
    	
    	try {
            Context context = new InitialContext();
            ValidatorFactory fac = Validation.byDefaultProvider()
				.providerResolver(() -> Arrays.asList(new HibernateValidator()))
				.configure()
				.buildValidatorFactory();
            context.rebind("java:comp/ValidatorFactory", fac); //$NON-NLS-1$
            try {
            	delegate.service(request, response);
            } finally {
            	try {
            		context.unbind("java:comp/ValidatorFactory"); //$NON-NLS-1$
            	} catch(NamingException e) {
            		// Ignore unbind exceptions
            	}
            }
    	} catch (NamingException e) {
			throw new ServletException(e);
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
	
	private void initCdi(HttpServletRequest request) {
		if(request.getAttribute(KEY_CDI_STORAGE) == null) {
			BoundRequestContext context = (BoundRequestContext)CDI.current().select(RequestContext.class, BoundLiteral.INSTANCE).get();
			Map<String, Object> cdiScope = new HashMap<>();
			request.setAttribute(KEY_CDI_STORAGE, cdiScope);
			context.associate(cdiScope);
			context.activate();
		}
	}
	@SuppressWarnings("unchecked")
	private void termCdi(HttpServletRequest request) {
		BoundRequestContext context = (BoundRequestContext)CDI.current().select(RequestContext.class, BoundLiteral.INSTANCE).get();
		context.invalidate();
		context.deactivate();
		context.dissociate((Map<String, Object>)request.getAttribute(KEY_CDI_STORAGE));
	}
}
