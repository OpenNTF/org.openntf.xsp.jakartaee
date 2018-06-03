package org.openntf.xsp.cdi.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ConversationScoped;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import org.jboss.weld.manager.BeanManagerImpl;
import org.openntf.xsp.cdi.CDILibrary;
import org.openntf.xsp.cdi.util.ContainerUtil;

import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.application.ViewHandlerExImpl;
import com.ibm.xsp.component.UIViewRootEx;

public class WeldViewHandler extends ViewHandlerExImpl {
	// TODO remove these when the view no longer exists
	
	private static final Map<String, ViewScopeContext> contexts = new ConcurrentHashMap<>();
	
	private static class ViewScopeContext extends AbstractIdentifiedContext {
		protected ViewScopeContext(String contextId, String viewId) {
			super(contextId, viewId, ConversationScoped.class);
		}
		
		@Override
		public boolean isActive() {
			if(!super.isActive()) {
				return false;
			}
			
			// Check the active session
			FacesContext facesContext = FacesContext.getCurrentInstance();
			if(facesContext != null) {
				UIViewRootEx viewRoot = (UIViewRootEx)facesContext.getViewRoot();
				String viewId = viewRoot.getUniqueViewId();
				return getId().equals(viewId);
			}
			
			return true;
		}
		
	}

	public WeldViewHandler(ViewHandler delegate) {
		super(delegate);
	}
	
	@Override
	public UIViewRoot createView(FacesContext facesContext, String pageName) {
		UIViewRootEx viewRoot = (UIViewRootEx)super.createView(facesContext, pageName);
		
		ApplicationEx application = ApplicationEx.getInstance(facesContext);
		if(CDILibrary.usesLibrary(application)) {
			String viewId = viewRoot.getUniqueViewId();
			BeanManagerImpl beanManager = ContainerUtil.getBeanManager(application);
			
			beanManager.addContext(contexts.compute(viewId, (id, old) -> 
				new ViewScopeContext(beanManager.getContextId(), viewId)
			));
		}
		
		return viewRoot;
	}
	
	@Override
	public UIViewRoot restoreView(FacesContext facesContext, String pageName) {
		// TODO Auto-generated method stub
		return super.restoreView(facesContext, pageName);
	}

}
