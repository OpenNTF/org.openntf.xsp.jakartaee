package org.openntf.xsp.jakartaee.module.jakarta;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.runtime.domino.adapter.servlet.LCDAdapterServletContext;

/**
 * This class wraps an old-style {@link ServletContext} to allow for
 * extending base behavior referenced by {@code ComponentModule}.
 * 
 * @since 3.5.0
 */
public class JakartaDelegatingServletContext extends LCDAdapterServletContext {
	private final LCDAdapterServletContext delegate;
	
	public JakartaDelegatingServletContext(LCDAdapterServletContext delegate) {
		super(delegate.getComponentModule(), new Hashtable<>());
		this.delegate = delegate;
	}
	
	@Override
	public AbstractJakartaModule getComponentModule() {
		return (AbstractJakartaModule)delegate.getComponentModule();
	}

	@Override
	public Object getAttribute(String arg0) {
		return delegate.getAttribute(arg0);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getAttributeNames() {
		return delegate.getAttributeNames();
	}

	@Override
	public ServletContext getContext(String arg0) {
		return delegate.getContext(arg0);
	}

	@Override
	public String getContextPath() {
		return delegate.getContextPath();
	}

	@Override
	public String getInitParameter(String arg0) {
		return delegate.getInitParameter(arg0);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getInitParameterNames() {
		return delegate.getInitParameterNames();
	}

	@Override
	public int getMajorVersion() {
		return delegate.getMajorVersion();
	}

	@Override
	public String getMimeType(String fileName) {
		String base = getComponentModule().getMimeType(fileName)
			.orElseGet(() -> delegate.getMimeType(fileName));
		
		if(StringUtil.isEmpty(base)) {
			Path path = Paths.get(fileName.replace("/", FileSystems.getDefault().getSeparator())); //$NON-NLS-1$
			Path filePath = path.getFileName();
			if(filePath != null) {
				try {
					return Files.probeContentType(filePath);
				} catch (IOException e) {
					e.printStackTrace();
					// Ignore
				}
			}
			
			return null;
		} else {
			return base;
		}
	}

	@Override
	public int getMinorVersion() {
		return delegate.getMinorVersion();
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String arg0) {
		return delegate.getNamedDispatcher(arg0);
	}

	@Override
	public String getRealPath(String arg0) {
		return delegate.getRealPath(arg0);
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		return delegate.getRequestDispatcher(arg0);
	}

	@Override
	public URL getResource(String arg0) throws MalformedURLException {
		return delegate.getResource(arg0);
	}

	@Override
	public InputStream getResourceAsStream(String arg0) {
		return delegate.getResourceAsStream(arg0);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Set getResourcePaths(String arg0) {
		return delegate.getResourcePaths(arg0);
	}

	@Override
	public String getServerInfo() {
		return delegate.getServerInfo();
	}

	@Override
	public Servlet getServlet(String arg0) throws ServletException {
		return delegate.getServlet(arg0);
	}

	@Override
	public String getServletContextName() {
		return delegate.getServletContextName();
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public Enumeration getServletNames() {
		return delegate.getServletNames();
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public Enumeration getServlets() {
		return delegate.getServlets();
	}

	@Override
	public void log(Exception arg0, String arg1) {
		delegate.log(arg0, arg1);
	}

	@Override
	public void log(String arg0, Throwable arg1) {
		delegate.log(arg0, arg1);
	}

	@Override
	public void log(String arg0) {
		delegate.log(arg0);
	}

	@Override
	public void removeAttribute(String arg0) {
		delegate.removeAttribute(arg0);
	}

	@Override
	public void setAttribute(String arg0, Object arg1) {
		delegate.setAttribute(arg0, arg1);
	}
}
