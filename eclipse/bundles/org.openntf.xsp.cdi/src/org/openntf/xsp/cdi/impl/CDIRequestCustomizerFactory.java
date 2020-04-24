package org.openntf.xsp.cdi.impl;

import java.util.HashMap;

import javax.enterprise.inject.spi.CDI;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.bound.BoundLiteral;
import org.jboss.weld.context.bound.BoundRequestContext;
import org.openntf.xsp.cdi.CDILibrary;
import org.openntf.xsp.cdi.context.SessionScopeContext;
import org.openntf.xsp.cdi.util.ContainerUtil;
import org.openntf.xsp.jakartaee.LibraryUtil;

import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.context.FacesContextEx;
import com.ibm.xsp.context.RequestCustomizerFactory;
import com.ibm.xsp.context.RequestParameters;
import com.ibm.xsp.event.FacesContextListener;

/**
 * This simulates a "request listener" to build and tear down
 * a request context.
 * 
 * @author Jesse Gallagher
 * @since 1.2.0
 */
public class CDIRequestCustomizerFactory extends RequestCustomizerFactory {
	
	/**
	 * @since 1.2.0
	 */
	private enum RequestTermListener implements FacesContextListener {
		instance;

		@Override
		public void beforeRenderingPhase(FacesContext facesContext) {
		}
		
		@Override
		public void beforeContextReleased(FacesContext facesContext) {
			CDI<Object> cdi = ContainerUtil.getContainer(ApplicationEx.getInstance(facesContext));
			BoundRequestContext context = (BoundRequestContext)cdi.select(RequestContext.class, BoundLiteral.INSTANCE).get();
			context.deactivate();
			context.invalidate();
			context.dissociate(null);
		}
	}

	@Override
	public void initializeParameters(FacesContext context, RequestParameters requestParameters) {
		FacesContextEx facesContext = (FacesContextEx)context;
		ApplicationEx app = ApplicationEx.getInstance(context);
		if(LibraryUtil.usesLibrary(CDILibrary.LIBRARY_ID, app)) {
			CDI<Object> cdi = ContainerUtil.getContainer(app);
			BoundRequestContext requestContext = (BoundRequestContext)cdi.select(RequestContext.class, BoundLiteral.INSTANCE).get();
			if(!requestContext.isActive()) {
				requestContext.associate(new HashMap<>());
				requestContext.activate();
			}
			
			facesContext.addRequestListener(RequestTermListener.instance);
			
			// Also inject the session scope, as XPages may not have called the SessionListener yet
			HttpServletRequest req = (HttpServletRequest)facesContext.getExternalContext().getRequest();
			SessionScopeContext.inject(app, req.getSession());
		}
	}

}
