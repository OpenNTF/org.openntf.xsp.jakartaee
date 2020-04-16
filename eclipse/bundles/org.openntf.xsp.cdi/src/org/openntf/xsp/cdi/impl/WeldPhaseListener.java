/**
 * Copyright © 2018-2020 Jesse Gallagher
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
package org.openntf.xsp.cdi.impl;

import java.util.HashMap;

import javax.enterprise.inject.spi.CDI;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.bound.BoundLiteral;
import org.jboss.weld.context.bound.BoundRequestContext;
import org.openntf.xsp.cdi.util.ContainerUtil;

import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.context.FacesContextEx;
import com.ibm.xsp.event.FacesContextListener;

/**
 * This simulates a "request listener" to build and tear down
 * a request context.
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class WeldPhaseListener implements PhaseListener {
	private static final long serialVersionUID = 1L;
	
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
	public void beforePhase(PhaseEvent event) {
		if(PhaseId.RESTORE_VIEW.equals(event.getPhaseId()) || PhaseId.RENDER_RESPONSE.equals(event.getPhaseId())) {
			FacesContextEx facesContext = (FacesContextEx)event.getFacesContext();
			CDI<Object> cdi = ContainerUtil.getContainer(ApplicationEx.getInstance(facesContext));
			BoundRequestContext context = (BoundRequestContext)cdi.select(RequestContext.class, BoundLiteral.INSTANCE).get();
			if(!context.isActive()) {
				context.associate(new HashMap<>());
				context.activate();
			}
			
			facesContext.addRequestListener(RequestTermListener.instance);
		}
	}
	
	@Override
	public void afterPhase(PhaseEvent event) {
		
	}


	@Override
	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}

}
