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

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;

import org.jboss.weld.context.http.HttpRequestContextImpl;
import org.jboss.weld.manager.BeanManagerImpl;
import org.openntf.xsp.cdi.util.ContainerUtil;

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
		BeanManagerImpl manager = ContainerUtil.getBeanManager(application);
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
			BeanManagerImpl beanManager = ContainerUtil.getBeanManager(application);
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
