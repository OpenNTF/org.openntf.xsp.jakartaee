package org.openntf.xsp.jakarta.servlet.nsf;

import java.io.IOException;
import java.util.Enumeration;

import org.openntf.xsp.jakartaee.AbstractXspLifecycleServlet;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.xsp.application.ApplicationEx;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Wraps a provided {@link Servlet} implementation with behavior to participate
 * in the XPages request lifecycle.
 * 
 * @author Jesse Gallagher
 * @since 2.5.0
 */
public class XspServletWrapper extends AbstractXspLifecycleServlet {
	private static final long serialVersionUID = 1L;
	
	private final HttpServlet delegate;
	
	public XspServletWrapper(ComponentModule module, Servlet delegate) {
		super(module);
		this.delegate = (HttpServlet)delegate;
	}
	
	@Override
	public void init() throws ServletException {
		super.init();
		delegate.init();
	}

	@Override
	protected void doInit(ServletConfig config) throws ServletException {
		delegate.init(config);
	}

	@Override
	protected void doService(HttpServletRequest request, HttpServletResponse response, ApplicationEx application)
			throws ServletException, IOException {
		delegate.service(request, response);
	}
	
	@Override
	public void destroy() {
		super.destroy();
		delegate.destroy();
	}

	@Override
	public String getInitParameter(String name) {
		return delegate.getInitParameter(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return delegate.getInitParameterNames();
	}

	@Override
	public String getServletInfo() {
		return delegate.getServletInfo();
	}

	@Override
	public void log(String msg) {
		delegate.log(msg);
	}

	@Override
	public void log(String message, Throwable t) {
		delegate.log(message, t);
	}

	@Override
	public String getServletName() {
		return delegate.getServletName();
	}

}
