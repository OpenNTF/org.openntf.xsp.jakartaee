/**
 * Copyright © 2020 Jesse Gallagher
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
package org.openntf.xsp.cdi.context;

import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;

import org.jboss.weld.manager.BeanManagerImpl;
import org.openntf.xsp.cdi.util.ContainerUtil;

import com.ibm.xsp.application.ApplicationEx;

public class RequestContext extends AbstractIdentifiedContext {
	public static final String CACHE_KEY = RequestContext.class.getName();

	public RequestContext(String contextId) {
		super(contextId, null, RequestScoped.class);
	}
	
	public static void inject() {
		ApplicationEx application = ApplicationEx.getInstance();
		BeanManagerImpl manager = ContainerUtil.getBeanManager(application);
		if(!manager.isContextActive(RequestScoped.class)) {
			// Build up the request context
			@SuppressWarnings("unchecked")
			Map<String, Object> requestScope = FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
			manager.addContext((RequestContext)requestScope.compute(RequestContext.CACHE_KEY, (key, val) -> new RequestContext(manager.getContextId())));
		}
	}
	
	public static void eject() {
		// Tear down the request context
		@SuppressWarnings("unchecked")
		Map<String, Object> requestScope = FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
		RequestContext context = (RequestContext)requestScope.get(RequestContext.CACHE_KEY);
		if(context != null) {
			context.invalidate();
		}
	}
}