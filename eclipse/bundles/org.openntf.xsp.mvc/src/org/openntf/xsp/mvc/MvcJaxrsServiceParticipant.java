package org.openntf.xsp.mvc;

import java.io.IOException;

import org.openntf.xsp.jaxrs.ServiceParticipant;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This {@link ServiceParticipant} object stashes the current Servlet request and
 * response objects during a JAX-RS request for use in Krazo beans.
 * 
 * @author Jesse Gallagher
 * @since 2.1.0
 */
public class MvcJaxrsServiceParticipant implements ServiceParticipant {
	public static final ThreadLocal<HttpServletRequest> CURRENT_REQUEST = new ThreadLocal<>();
	public static final ThreadLocal<HttpServletResponse> CURRENT_RESPONSE = new ThreadLocal<>();

	@Override
	public void doBeforeService(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		CURRENT_REQUEST.set(request);
		CURRENT_RESPONSE.set(response);
	}

	@Override
	public void doAfterService(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		CURRENT_REQUEST.set(null);
		CURRENT_RESPONSE.set(null);
	}

}
