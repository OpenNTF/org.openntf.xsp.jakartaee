/**
 * Copyright © 2018-2022 Martin Pradny and Jesse Gallagher
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
package org.openntf.xsp.jaxrs.impl;

import java.io.IOException;
import java.util.List;

import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.openntf.xsp.cdi.ext.CDIConstants;
import org.openntf.xsp.jakartaee.AbstractXspLifecycleServlet;
import org.openntf.xsp.jaxrs.ServiceParticipant;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.xsp.application.ApplicationEx;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
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
public class FacesJAXRSServletContainer extends AbstractXspLifecycleServlet {
	private static final long serialVersionUID = 1L;
	
	private final HttpServletDispatcher delegate;

	public FacesJAXRSServletContainer(ComponentModule module) {
		super(module);
		this.delegate = new HttpServletDispatcher();
	}
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setAttribute(CDIConstants.CDI_JAXRS_REQUEST, "true"); //$NON-NLS-1$
		super.service(request, response);
	}
	
	@Override
	protected void doInit(ServletConfig config) throws ServletException {
		delegate.init(config);
	}
	
	@Override
	protected void doService(HttpServletRequest request, HttpServletResponse response, ApplicationEx application) throws ServletException, IOException {
		@SuppressWarnings("unchecked")
		List<ServiceParticipant> participants = (List<ServiceParticipant>)application.findServices(ServiceParticipant.EXTENSION_POINT);
    	for(ServiceParticipant participant : participants) {
    		participant.doBeforeService(request, response);
    	}
    	
    	try {
    		delegate.service(request, response);
    	} finally {
    		for(ServiceParticipant participant : participants) {
	    		participant.doAfterService(request, response);
	    	}
    	}
	}
	
	@Override
	public void destroy() {
		super.destroy();
		delegate.destroy();
	}
}
