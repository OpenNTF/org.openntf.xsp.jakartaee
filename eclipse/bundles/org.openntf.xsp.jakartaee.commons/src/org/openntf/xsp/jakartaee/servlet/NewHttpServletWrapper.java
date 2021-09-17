package org.openntf.xsp.jakartaee.servlet;

import java.io.IOException;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;

public class NewHttpServletWrapper extends javax.servlet.http.HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private Servlet delegate;
	
	public NewHttpServletWrapper(Servlet delegate) {
		this.delegate = delegate;
	}

	@Override
	public void destroy() {
		delegate.destroy();
	}

	@Override
	public javax.servlet.ServletConfig getServletConfig() {
		ServletConfig result = delegate.getServletConfig();
		return new NewServletConfigWrapper(result);
	}

	@Override
	public String getServletInfo() {
		return delegate.getServletInfo();
	}

	@Override
	public void init(javax.servlet.ServletConfig arg0) throws javax.servlet.ServletException {
		try {
			delegate.init(new OldServletConfigWrapper(arg0));
		} catch (ServletException e) {
			throw new javax.servlet.ServletException(e);
		}
	}

	@Override
	public void service(javax.servlet.ServletRequest arg0, javax.servlet.ServletResponse arg1) throws javax.servlet.ServletException, IOException {
		javax.servlet.http.HttpServletRequest req = (javax.servlet.http.HttpServletRequest)arg0;
		javax.servlet.http.HttpServletResponse resp = (javax.servlet.http.HttpServletResponse)arg1;
		
		NewServletContextWrapper context = new NewServletContextWrapper(delegate.getServletConfig().getServletContext());
		try {
			delegate.service(new OldHttpServletRequestWrapper(context, req), new OldHttpServletResponseWrapper(resp));
		} catch (ServletException e) {
			throw new javax.servlet.ServletException(e);
		}
	}
}
