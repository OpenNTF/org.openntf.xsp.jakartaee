package org.openntf.xsp.jakartaee.servlet;

import java.io.IOException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class OldRequestDispatcherWrapper implements RequestDispatcher {
	private final javax.servlet.RequestDispatcher delegate;
	
	public OldRequestDispatcherWrapper(javax.servlet.RequestDispatcher delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse resp = (HttpServletResponse)response;
		try {
			this.delegate.forward(new NewHttpServletRequestWrapper(req), new NewHttpServletResponseWrapper(resp));
		} catch (javax.servlet.ServletException e) {
			throw new ServletException(e);
		}
	}

	@Override
	public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse resp = (HttpServletResponse)response;
		try {
			this.delegate.forward(new NewHttpServletRequestWrapper(req), new NewHttpServletResponseWrapper(resp));
		} catch (javax.servlet.ServletException e) {
			throw new ServletException(e);
		}
	}

}
