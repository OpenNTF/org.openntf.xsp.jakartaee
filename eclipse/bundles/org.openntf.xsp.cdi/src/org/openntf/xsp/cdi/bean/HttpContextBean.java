package org.openntf.xsp.cdi.bean;

import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Provides HTTP contextual objects when available.
 * 
 * @since 2.16.0
 */
@RequestScoped
public class HttpContextBean {
	private static ThreadLocal<HttpServletResponse> THREAD_RESPONSES = new ThreadLocal<>();
	
	public static void setThreadResponse(HttpServletResponse response) {
		THREAD_RESPONSES.set(response);
	}
	
	@Produces
	public HttpServletRequest getServletRequest() {
		return ComponentModuleLocator.getDefault()
			.flatMap(ComponentModuleLocator::getServletRequest)
			.orElse(null);
	}
	
	@Produces
	public HttpServletResponse getServletResponse() {
		return THREAD_RESPONSES.get();
	}
	
	@Produces
	public ServletContext getServletContext() {
		return getServletRequest().getServletContext();
	}
}
