package org.openntf.xsp.jaxrs.impl;

import javax.faces.context.FacesContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.ibm.xsp.controller.FacesController;
import com.ibm.xsp.webapp.DesignerFacesServlet;

/**
 * This subclass of {@link DesignerFacesServlet} is intended to act as a delegating
 * peer to {@link FacesJAXRSServletContainer} to assist in managing the Faces
 * lifecycle, elevating the visibility of utility methods.
 * 
 * @author Jesse Gallagher
 * @since 2.3.0
 */
public class PeerFacesServlet extends DesignerFacesServlet {
	@Override
	public FacesContext getFacesContext(ServletRequest paramServletRequest, ServletResponse paramServletResponse) {
		return super.getFacesContext(paramServletRequest, paramServletResponse);
	}
	
	@Override
	public FacesController getContextFacesController() {
		return super.getContextFacesController();
	}
}
