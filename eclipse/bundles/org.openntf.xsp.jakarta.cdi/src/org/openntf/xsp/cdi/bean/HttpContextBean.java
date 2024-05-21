package org.openntf.xsp.cdi.bean;

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
	static ThreadLocal<HttpServletResponse> THREAD_RESPONSES = new ThreadLocal<>();
	
	public static void setThreadResponse(HttpServletResponse response) {
		THREAD_RESPONSES.set(response);
	}
	
	// Oddly, starting with version 3.0 (JEE 10), just returning the request
	//   and response directly leads to MVC re-using the same object across
	//   multiple requests. Use these constantly-proxying objects instead to
	//   avoid the trouble.
	// TODO figure out why this happens, when it didn't in the JEE 9 versions
	
	@Produces
	public HttpServletRequest getServletRequest() {
		return ProxyingHttpServletRequest.INSTANCE;
	}
	
	@Produces
	public HttpServletResponse getServletResponse() {
		return ProxyingHttpServletResponse.INSTANCE;
	}
	
	@Produces
	public ServletContext getServletContext() {
		return getServletRequest().getServletContext();
	}
}
