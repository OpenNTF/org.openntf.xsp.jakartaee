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

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.jboss.weld.manager.BeanManagerImpl;
import org.openntf.xsp.cdi.context.AbstractIdentifiedContext;
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

	private static class RequestContext extends AbstractIdentifiedContext {
		public RequestContext(String contextId) {
			super(contextId, null, RequestScoped.class);
		}
	}
	
	private static final String CACHE_KEY = WeldPhaseListener.class.getName();

	@SuppressWarnings("unchecked")
	@Override
	public void beforePhase(PhaseEvent event) {
		ApplicationEx application = ApplicationEx.getInstance();
		BeanManagerImpl manager = ContainerUtil.getBeanManager(application);
		if(!manager.isContextActive(RequestScoped.class)) {
			// Build up the request context
			Map<String, Object> requestScope = FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
			manager.addContext((RequestContext)requestScope.compute(CACHE_KEY, (key, val) -> new RequestContext(manager.getContextId())));
			
		}
	}
	
	@Override
	public void afterPhase(PhaseEvent event) {
		if(PhaseId.RENDER_RESPONSE.equals(event.getPhaseId())) {
			// Tear down the request context
			@SuppressWarnings("unchecked")
			Map<String, Object> requestScope = FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
			RequestContext context = (RequestContext)requestScope.get(CACHE_KEY);
			if(context != null) {
				context.invalidate();
			}
		}
	}


	@Override
	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}

}
