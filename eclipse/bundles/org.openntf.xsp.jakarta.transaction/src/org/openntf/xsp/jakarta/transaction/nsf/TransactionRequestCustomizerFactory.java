package org.openntf.xsp.jakarta.transaction.nsf;

import javax.faces.context.FacesContext;

import org.openntf.xsp.jakarta.transaction.servlet.AbstractTransactionJndiConfigurator;

import com.ibm.xsp.context.FacesContextEx;
import com.ibm.xsp.context.RequestCustomizerFactory;
import com.ibm.xsp.context.RequestParameters;
import com.ibm.xsp.event.FacesContextListener;

/**
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public class TransactionRequestCustomizerFactory extends RequestCustomizerFactory implements AbstractTransactionJndiConfigurator {

	@Override
	public void initializeParameters(FacesContext facesContext, RequestParameters requestParameters) {
		if(facesContext instanceof FacesContextEx) {
			pushTransaction();
			
			((FacesContextEx)facesContext).addRequestListener(new FacesContextListener() {
				@Override
				public void beforeRenderingPhase(FacesContext paramFacesContext) {
					// NOP
				}
				
				@Override
				public void beforeContextReleased(FacesContext paramFacesContext) {
					popTransaction();
				}
			});
		}
	}

}
