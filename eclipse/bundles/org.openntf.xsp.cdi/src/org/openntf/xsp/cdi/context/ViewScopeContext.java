package org.openntf.xsp.cdi.context;

import java.io.Serializable;

import javax.enterprise.context.ConversationScoped;
import javax.faces.context.FacesContext;

import com.ibm.xsp.component.UIViewRootEx;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.2.0
 */
public class ViewScopeContext extends AbstractIdentifiedContext implements Serializable {
	private static final long serialVersionUID = 1L;

	public ViewScopeContext(String contextId, String uuid) {
		super(contextId, uuid, ConversationScoped.class);
	}
	
	@Override
	public boolean isActive() {
		if(!super.isActive()) {
			return false;
		}
		
		// Check the active view
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if(facesContext != null) {
			UIViewRootEx viewRoot = ((UIViewRootEx)facesContext.getViewRoot());
			if(viewRoot != null) {
				String viewId = viewRoot.getUniqueViewId();
				return getId().equals(viewId);
			}
		}
		
		return false;
	}
	
}