package org.openntf.xsp.mvc.bean;

import org.openntf.xsp.mvc.MvcJaxrsServiceParticipant;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Provides CDI access to common Servlet artifacts
 * 
 * @author Jesse Gallagher
 * @since 2.1.0
 */
@ApplicationScoped
public class DominoHttpContextBean {
	@Produces
	@RequestScoped
	public HttpServletRequest getServletRequest() {
		return MvcJaxrsServiceParticipant.CURRENT_REQUEST.get();
	}
	
	@Produces
	@RequestScoped
	public HttpServletResponse getServletResponse() {
		return MvcJaxrsServiceParticipant.CURRENT_RESPONSE.get();
	}
	
	@Produces
	@RequestScoped
	public ServletContext getServletContext() {
		return getServletRequest().getServletContext();
	}
}
