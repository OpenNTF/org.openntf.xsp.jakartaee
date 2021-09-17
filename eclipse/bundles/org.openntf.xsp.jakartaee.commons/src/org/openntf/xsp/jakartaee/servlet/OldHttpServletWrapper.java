package org.openntf.xsp.jakartaee.servlet;

import java.io.IOException;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class OldHttpServletWrapper extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private final javax.servlet.Servlet delegate;
	
	public OldHttpServletWrapper(javax.servlet.Servlet delegate) {
		this.delegate = delegate;
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		try {
			delegate.init(new NewServletConfigWrapper(config));
		} catch (javax.servlet.ServletException e) {
			throw new ServletException(e);
		}
	}

	@Override
	public ServletConfig getServletConfig() {
		javax.servlet.ServletConfig result = delegate.getServletConfig();
		return ((NewServletConfigWrapper)result).getDelegate();
	}

	@Override
	public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
		HttpServletRequest req = (HttpServletRequest)request;
		HttpServletResponse resp = (HttpServletResponse)response;
		try {
			delegate.service(new NewHttpServletRequestWrapper(req), new NewHttpServletResponseWrapper(resp));
		} catch (javax.servlet.ServletException e) {
			throw new ServletException(e);
		}
	}

	@Override
	public String getServletInfo() {
		return delegate.getServletInfo();
	}

	@Override
	public void destroy() {
		delegate.destroy();
	}

}
