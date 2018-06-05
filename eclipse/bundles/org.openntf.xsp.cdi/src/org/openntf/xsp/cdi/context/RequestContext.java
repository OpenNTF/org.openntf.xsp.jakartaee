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