package org.openntf.xsp.jakarta.concurrency.nsf;

import javax.faces.context.FacesContext;

import org.openntf.xsp.jakarta.concurrency.servlet.AbstractServletJndiConfigurator;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;

import com.ibm.xsp.context.FacesContextEx;
import com.ibm.xsp.context.RequestCustomizerFactory;
import com.ibm.xsp.context.RequestParameters;
import com.ibm.xsp.event.FacesContextListener;

import jakarta.servlet.ServletContext;

/**
 * Sets and unsets the application's managed executors during an XPages request.
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public class NSFConcurrencyRequestCustomizerFactory extends RequestCustomizerFactory implements AbstractServletJndiConfigurator {

	@Override
	public void initializeParameters(FacesContext facesContext, RequestParameters requestParameters) {
		if(facesContext instanceof FacesContextEx) {
			javax.servlet.ServletContext oldContext = (javax.servlet.ServletContext)facesContext.getExternalContext().getContext();
			ServletContext servletContext = ServletUtil.oldToNew(facesContext.getExternalContext().getRequestContextPath(), oldContext, 2, 5);
			pushExecutors(servletContext);
			
			((FacesContextEx)facesContext).addRequestListener(new FacesContextListener() {
				@Override
				public void beforeRenderingPhase(FacesContext paramFacesContext) {
					// NOP
				}
				
				@Override
				public void beforeContextReleased(FacesContext paramFacesContext) {
					popExecutors(servletContext);
				}
			});
		}
		
		
	}

}
