package org.openntf.xsp.cdi.impl;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;

import org.jboss.weld.context.http.HttpRequestContextImpl;
import org.jboss.weld.manager.BeanManagerImpl;

import com.ibm.xsp.application.ApplicationEx;

/**
 * This simulates a "request listener" to build and tear down
 * a request context.
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class WeldPhaseListener implements PhaseListener {
	private static final long serialVersionUID = 1L;


	@Override
	public void beforePhase(PhaseEvent event) {
		ApplicationEx application = ApplicationEx.getInstance();
		BeanManagerImpl manager = WeldApplicationListener.getBeanManager(application);
		if(!manager.isContextActive(RequestScoped.class)) {
			// Build up the request context
			HttpRequestContextImpl requestScope = new HttpRequestContextImpl(manager.getContextId());
			HttpServletRequest req = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
			requestScope.associate(req);
			requestScope.activate();
			manager.addContext(requestScope);
			
		}
	}
	
	@Override
	public void afterPhase(PhaseEvent event) {
		if(PhaseId.RENDER_RESPONSE.equals(event.getPhaseId())) {
			// Tear down the request context
			ApplicationEx application = ApplicationEx.getInstance();
			BeanManagerImpl beanManager = WeldApplicationListener.getBeanManager(application);
			HttpRequestContextImpl requestScope = (HttpRequestContextImpl)beanManager.getContext(RequestScoped.class);
			requestScope.invalidate();
			requestScope.deactivate();
		}
	}


	@Override
	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}

}
