package org.openntf.xsp.cdi.impl;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSessionEvent;

import org.jboss.weld.context.bound.BoundSessionContextImpl;
import org.jboss.weld.context.http.HttpSessionDestructionContext;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.manager.BeanManagerImpl;
import org.openntf.xsp.cdi.CDILibrary;

import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.application.events.SessionListener;

public class WeldSessionListener implements SessionListener {

	@SuppressWarnings("unchecked")
	@Override
	public void sessionCreated(ApplicationEx application, HttpSessionEvent event) {
		if(CDILibrary.usesLibrary(application)) {
			BeanManagerImpl beanManager = WeldApplicationListener.getBeanManager(application);
			HttpSessionDestructionContext sessionScope = new HttpSessionDestructionContext(beanManager.getContextId(), null);
			sessionScope.associate(event.getSession());
			sessionScope.activate();
			beanManager.addContext(sessionScope);
		}
	}

	@Override
	public void sessionDestroyed(ApplicationEx application, HttpSessionEvent event) {
		if(CDILibrary.usesLibrary(application)) {
			WeldContainer container = WeldApplicationListener.getContainer(application);
			BeanManager beanManager = container.getBeanManager();
			HttpSessionDestructionContext sessionScope = (HttpSessionDestructionContext)beanManager.getContext(SessionScoped.class);
			sessionScope.deactivate();
			sessionScope.dissociate(event.getSession());
		}
	}

}
