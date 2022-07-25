package org.openntf.xsp.jsp.webapp;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.stream.Collectors;

import org.apache.jasper.Constants;
import org.apache.jasper.servlet.JspServlet;
import org.apache.jasper.xmlparser.ParserUtils;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.jsp.EarlyInitFactory;
import org.openntf.xsp.jsp.util.DominoJspUtil;
import org.osgi.framework.BundleException;

import com.ibm.xsp.extlib.util.ExtLibUtil;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Legacy {@link HttpServlet} implementation that can be mapped to {@code *.jsp}
 * to provide JSP processing in a webapp.
 * 
 * @author Jesse Gallagher
 * @since 2.8.0
 */
public class WebappJspServlet extends javax.servlet.http.HttpServlet {

	private static final long serialVersionUID = 1L;

	private final JspServlet delegate;
	private ServletContext context;
	
	public WebappJspServlet() {
		this.delegate = new JspServlet();
	}
	
	@Override
	public void init(javax.servlet.ServletConfig config) throws javax.servlet.ServletException {
		super.init(config);
		
		this.context = ServletUtil.oldToNew(config.getServletContext().getContextPath(), config.getServletContext());
		
		try {
			String classpath = DominoJspUtil.buildBundleClassPath()
				.stream()
				.map(File::toString)
				.collect(Collectors.joining(DominoJspUtil.PATH_SEP));
			this.context.setInitParameter("classpath", classpath); //$NON-NLS-1$
			this.context.setInitParameter("development", Boolean.toString(ExtLibUtil.isDevelopmentMode())); //$NON-NLS-1$
	
			Path tempDir = LibraryUtil.getTempDirectory();
			tempDir = tempDir.resolve(getClass().getName());
			String moduleName = Integer.toString(System.identityHashCode(config.getServletContext()));
			tempDir = tempDir.resolve(moduleName);
			Files.createDirectories(tempDir);
			this.context.setInitParameter("scratchdir", tempDir.toString()); //$NON-NLS-1$
		} catch(IOException | BundleException e) {
			throw new javax.servlet.ServletException(e);
		}
		
		ClassLoader current = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(new URLClassLoader(new URL[0], current));
			try {
				delegate.init(ServletUtil.oldToNew(config));
			} catch (ServletException e) {
				throw ServletUtil.newToOld(e);
			}
		} finally {
			Thread.currentThread().setContextClassLoader(current);
		}
	}
	
	@Override
	public void service(javax.servlet.ServletRequest oldRequest, javax.servlet.ServletResponse oldResponse)
			throws javax.servlet.ServletException, IOException {
		try {
			HttpServletRequest request = ServletUtil.oldToNew(ServletUtil.newToOld(this.context), (javax.servlet.http.HttpServletRequest)oldRequest);
			HttpServletResponse response = ServletUtil.oldToNew((javax.servlet.http.HttpServletResponse)oldResponse);
			
			AccessController.doPrivileged((PrivilegedExceptionAction<Void>)() -> {
				
				//context.setAttribute("org.glassfish.jsp.beanManagerELResolver", NSFELResolver.instance); //$NON-NLS-1$
				context.setAttribute(Constants.JSP_TLD_URI_TO_LOCATION_MAP, DominoJspUtil.buildJstlDtdMap());
				
				ClassLoader current = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(DominoJspUtil.buildJspClassLoader(current));
				ServletUtil.getListeners(context, ServletRequestListener.class)
					.forEach(l -> l.requestInitialized(new ServletRequestEvent(context, request)));
				try {
					ParserUtils.setDtdResourcePrefix(EarlyInitFactory.getServletDtdPath().toUri().toString());
					delegate.service(request, response);
				} finally {
					ServletUtil.getListeners(context, ServletRequestListener.class)
						.forEach(l -> l.requestDestroyed(new ServletRequestEvent(context, request)));
					Thread.currentThread().setContextClassLoader(current);
					context.removeAttribute("org.glassfish.jsp.beanManagerELResolver"); //$NON-NLS-1$
					context.removeAttribute(Constants.JSP_TLD_URI_TO_LOCATION_MAP);
				}
				return null;
			});
		} catch(PrivilegedActionException e) {
			e.printStackTrace();
			Throwable cause = e.getCause();
			if(cause instanceof ServletException) {
				throw ServletUtil.newToOld((ServletException)cause);
			} else if(cause instanceof IOException) {
				throw (IOException)cause;
			} else {
				throw new javax.servlet.ServletException(e);
			}
		} catch(Throwable t) {
			t.printStackTrace();
			throw t;
		} finally {
			// Looks like Jasper doesn't flush this on its own
			oldResponse.getWriter().flush();
			oldResponse.flushBuffer();
		}
	}
	
	@Override
	public void destroy() {
		super.destroy();
		delegate.destroy();
	}
}
