/**
 * Copyright © 2018-2022 Contributors to the XPages Jakarta EE Support Project
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
