package org.openntf.xsp.cdi.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.weld.manager.BeanManagerImpl;
import org.openntf.xsp.cdi.util.ContainerUtil;

import com.ibm.xsp.application.ApplicationEx;

/**
 * @author Jesse Gallagher
 * @since 1.2.0
 */
public class SessionScopeContext extends AbstractIdentifiedContext {
	public static final Map<String, SessionScopeContext> contexts = new ConcurrentHashMap<>();
	
	public SessionScopeContext(String contextId, String key) {
		super(contextId, key, SessionScoped.class);
	}
	
	@Override
	public boolean isActive() {
		if(!super.isActive()) {
			return false;
		}
		
		// Check the active session
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if(facesContext != null) {
			ApplicationEx application = ApplicationEx.getInstance(facesContext);
			HttpServletRequest req = (HttpServletRequest)facesContext.getExternalContext().getRequest();
			HttpSession session = req.getSession();
			String key = application.getApplicationId() + session.getId();
			return getId().equals(key);
		}
		
		return false;
	}
	
	public static void inject(ApplicationEx application, HttpSession session) {
		String key = application.getApplicationId() + session.getId();
		contexts.computeIfAbsent(key, id -> {
			BeanManagerImpl beanManager = ContainerUtil.getBeanManager(application);
			
			SessionScopeContext context = new SessionScopeContext(beanManager.getContextId(), id);
			beanManager.addContext(context);
			return context;
		});
	}
	
	public static void eject(ApplicationEx application, HttpSession session) {
		String key = application.getApplicationId() + session.getId();
		SessionScopeContext context = contexts.remove(key);
		if(context != null) {
			context.invalidate();
		}
	}
	
}