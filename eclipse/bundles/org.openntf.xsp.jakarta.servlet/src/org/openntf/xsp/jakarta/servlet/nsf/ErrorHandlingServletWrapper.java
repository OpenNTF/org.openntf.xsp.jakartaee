package org.openntf.xsp.jakarta.servlet.nsf;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.designer.runtime.domino.adapter.util.XSPErrorPage;
import com.ibm.xsp.acl.NoAccessSignal;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This Servlet wrapper is similar to {@link XspServletWrapper} in its
 * exception handling, but does not participate in the XPages lifecycle.
 * 
 * @since 3.4.0
 */
public class ErrorHandlingServletWrapper extends HttpServlet {
	private static final Logger log = Logger.getLogger(ErrorHandlingServletWrapper.class.getPackageName());
	
	private final HttpServlet delegate;
	
	public ErrorHandlingServletWrapper(Servlet delegate) {
		this.delegate = (HttpServlet)delegate;
	}
	
	@Override
	public void init() throws ServletException {
		delegate.init();
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		delegate.init(config);
	}
	
	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		try {
			response.setBufferSize(0);

	    	delegate.service(request, response);
		} catch(NoAccessSignal t) {
			throw t;
		} catch(Throwable t) {
			if(log.isLoggable(Level.SEVERE)) {
				log.log(Level.SEVERE, "Encountered unhandled exception in Servlet", t);
			}

			try(PrintWriter w = response.getWriter()) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				XSPErrorPage.handleException(w, t, null, false);
			} catch (javax.servlet.ServletException e) {
				throw new IOException(e);
			} catch(IllegalStateException e) {
				// Happens when the writer or output has already been opened
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
	}
	
	@Override
	public void destroy() {
		super.destroy();
		delegate.destroy();
	}
	
	@Override
	public String getInitParameter(final String name) {
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
	public void log(final String msg) {
		delegate.log(msg);
	}

	@Override
	public void log(final String message, final Throwable t) {
		delegate.log(message, t);
	}

	@Override
	public String getServletName() {
		return delegate.getServletName();
	}
}
