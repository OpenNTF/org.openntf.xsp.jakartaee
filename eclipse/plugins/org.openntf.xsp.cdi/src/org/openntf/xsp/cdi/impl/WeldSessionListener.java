package org.openntf.xsp.cdi.impl;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

import org.jboss.weld.context.AbstractSharedContext;
import org.jboss.weld.manager.BeanManagerImpl;
import org.openntf.xsp.cdi.CDILibrary;

import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.application.events.SessionListener;

public class WeldSessionListener implements SessionListener {
	
	private static final Map<String, SessionScopeContext> contexts = new ConcurrentHashMap<>();
	
	private static class SessionScopeContext extends AbstractSharedContext {
		private final String sessionId;
		
		protected SessionScopeContext(String contextId, String sessionId) {
			super(contextId);
			this.sessionId = sessionId;
		}

		@Override
		public Class<? extends Annotation> getScope() {
			return SessionScoped.class;
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
				return sessionId.equals(session.getId());
			}
			
			return true;
		}
		
	}

	@Override
	public void sessionCreated(ApplicationEx application, HttpSessionEvent event) {
		if(CDILibrary.usesLibrary(application)) {
			String sessionId = event.getSession().getId();
			BeanManagerImpl beanManager = WeldApplicationListener.getBeanManager(application);
			
			beanManager.addContext(contexts.compute(sessionId, (id, old) -> 
				new SessionScopeContext(beanManager.getContextId(), sessionId)
			));
		}
	}

	@Override
	public void sessionDestroyed(ApplicationEx application, HttpSessionEvent event) {
		if(CDILibrary.usesLibrary(application)) {
			String sessionId = event.getSession().getId();
			SessionScopeContext context = contexts.get(sessionId);
			if(context != null) {
				context.invalidate();
			}
		}
	}

}
