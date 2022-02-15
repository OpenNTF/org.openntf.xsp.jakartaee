package org.openntf.xsp.test.beanbundle.servlet;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;

public class RootServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final HttpServletDispatcher delegate = new HttpServletDispatcher();
	private ServletContext context;
	
	@Override
	public void init() throws ServletException {
		super.init();
		try {
			delegate.init();
		} catch (jakarta.servlet.ServletException e) {
			throw new ServletException(e);
		}
	}
	
	@Override
	public void init(ServletConfig servletConfig) throws ServletException {
		this.context = servletConfig.getServletContext();
		
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
			Thread.currentThread().setContextClassLoader(RootServlet.class.getClassLoader());
			return null;
		});
		try {
			delegate.init(ServletUtil.oldToNew(servletConfig));
		} catch (jakarta.servlet.ServletException e) {
			throw new ServletException(e);
		} finally {
			AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
				Thread.currentThread().setContextClassLoader(cl);
				return null;
			});
		}
	}
	
	@Override
	public void service(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		jakarta.servlet.http.HttpServletRequest newReq = ServletUtil.oldToNew(this.context, request);

		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		try {
			AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
				Thread.currentThread().setContextClassLoader(RootServlet.class.getClassLoader());

				return null;
			});

			delegate.service(newReq, ServletUtil.oldToNew(response));
		} catch(Exception e) {
			throw new ServletException(e);
		} finally {
			AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
				Thread.currentThread().setContextClassLoader(cl);
				return null;
			});
		}
	}
}
