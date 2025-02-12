/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
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

import com.ibm.xsp.context.FacesContextEx;
import com.ibm.xsp.context.RequestCustomizerFactory;
import com.ibm.xsp.context.RequestParameters;
import com.ibm.xsp.event.FacesContextListener;

import org.openntf.xsp.jakarta.transaction.servlet.AbstractTransactionJndiConfigurator;

/**
 *
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public class TransactionRequestCustomizerFactory extends RequestCustomizerFactory implements AbstractTransactionJndiConfigurator {

	@Override
	public void initializeParameters(final FacesContext facesContext, final RequestParameters requestParameters) {
		if(facesContext instanceof FacesContextEx) {
			pushTransaction();

			((FacesContextEx)facesContext).addRequestListener(new FacesContextListener() {
				@Override
				public void beforeRenderingPhase(final FacesContext paramFacesContext) {
					// NOP
				}

				@Override
				public void beforeContextReleased(final FacesContext paramFacesContext) {
					popTransaction();
				}
			});
		}
	}

}
