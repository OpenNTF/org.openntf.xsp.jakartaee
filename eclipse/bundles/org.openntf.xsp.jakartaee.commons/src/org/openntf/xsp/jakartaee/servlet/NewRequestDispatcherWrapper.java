package org.openntf.xsp.jakartaee.servlet;

import java.io.IOException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;

public class NewRequestDispatcherWrapper implements javax.servlet.RequestDispatcher {
	private final RequestDispatcher delegate;
	
	public NewRequestDispatcherWrapper(RequestDispatcher delegate) {
		this.delegate = delegate;
	}

	@Override
	public void forward(javax.servlet.ServletRequest arg0, javax.servlet.ServletResponse arg1) throws javax.servlet.ServletException, IOException {
		javax.servlet.http.HttpServletRequest req = (javax.servlet.http.HttpServletRequest)arg0;
		javax.servlet.http.HttpServletResponse resp = (javax.servlet.http.HttpServletResponse)arg1;
		try {
			delegate.forward(new OldHttpServletRequestWrapper(null, req), new OldHttpServletResponseWrapper(resp));
		} catch (ServletException e) {
			throw new javax.servlet.ServletException(e);
		}
	}

	@Override
	public void include(javax.servlet.ServletRequest arg0, javax.servlet.ServletResponse arg1) throws javax.servlet.ServletException, IOException {
		javax.servlet.http.HttpServletRequest req = (javax.servlet.http.HttpServletRequest)arg0;
		javax.servlet.http.HttpServletResponse resp = (javax.servlet.http.HttpServletResponse)arg1;
		try {
			delegate.include(new OldHttpServletRequestWrapper(null, req), new OldHttpServletResponseWrapper(resp));
		} catch (ServletException e) {
			throw new javax.servlet.ServletException(e);
		}
	}
}
