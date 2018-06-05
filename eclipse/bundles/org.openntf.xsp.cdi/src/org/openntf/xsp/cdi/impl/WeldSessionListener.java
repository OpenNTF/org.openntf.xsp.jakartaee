/**
 * Copyright © 2018 Jesse Gallagher
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
package org.openntf.xsp.cdi.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

import org.jboss.weld.manager.BeanManagerImpl;
import org.openntf.xsp.cdi.CDILibrary;
import org.openntf.xsp.cdi.context.AbstractIdentifiedContext;
import org.openntf.xsp.cdi.util.ContainerUtil;
import org.openntf.xsp.jakartaee.LibraryUtil;

import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.application.events.SessionListener;

public class WeldSessionListener implements SessionListener {
	
	private static final Map<String, SessionScopeContext> contexts = new ConcurrentHashMap<>();
	
	private static class SessionScopeContext extends AbstractIdentifiedContext {
		protected SessionScopeContext(String contextId, String sessionId) {
			super(contextId, sessionId, SessionScoped.class);
		}
		
		@Override
		public boolean isActive() {
			if(!super.isActive()) {
				return false;
			}
			
			// Check the active session
			FacesContext facesContext = FacesContext.getCurrentInstance();
			if(facesContext != null) {
				HttpServletRequest req = (HttpServletRequest)facesContext.getExternalContext().getRequest();
				HttpSession session = req.getSession();
				return getId().equals(session.getId());
			}
			
			return true;
		}
		
	}

	@Override
	public void sessionCreated(ApplicationEx application, HttpSessionEvent event) {
		if(LibraryUtil.usesLibrary(CDILibrary.LIBRARY_ID, application)) {
			String sessionId = event.getSession().getId();
			BeanManagerImpl beanManager = ContainerUtil.getBeanManager(application);
			
			beanManager.addContext(contexts.compute(sessionId, (id, old) -> 
				new SessionScopeContext(beanManager.getContextId(), sessionId)
			));
		}
	}

	@Override
	public void sessionDestroyed(ApplicationEx application, HttpSessionEvent event) {
		if(LibraryUtil.usesLibrary(CDILibrary.LIBRARY_ID, application)) {
			String sessionId = event.getSession().getId();
			SessionScopeContext context = contexts.get(sessionId);
			if(context != null) {
				context.invalidate();
			}
		}
	}

}
