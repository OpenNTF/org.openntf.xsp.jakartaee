package org.openntf.xsp.jakarta.webcontainer.osgi;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.openntf.xsp.jakartaee.servlet.ServletUtil;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.osgi.framework.Bundle;

import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.StreamUtil;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;
import com.ibm.designer.runtime.domino.adapter.util.XSPErrorPage;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletRequestAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletResponseAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpSessionAdapter;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.HttpHeaders;

/**
 * @author Jesse Gallagher
 * @since 2.8.0
 */
public class JakartaContainerModule extends ComponentModule {
	private final Bundle bundle;
	private final String contextRoot;
	private final Optional<String> contentLocation;
	
	private ServletContext servletContext;

	public JakartaContainerModule(LCDEnvironment env, JakartaContainerService service, Bundle bundle, String contextRoot, String contentLocation) {
		super(env, service, JakartaContainerModule.class.getSimpleName() + ": " + contextRoot, true); //$NON-NLS-1$
		this.bundle = bundle;
		this.contextRoot = contextRoot;
		if(StringUtil.isNotEmpty(contentLocation)) {
			this.contentLocation = Optional.of(contentLocation);
		} else {
			this.contentLocation = Optional.empty();
		}
		
		// TODO set jakarta.servlet.context.tempdir from parent-set javax.servlet.context.tempdir
	}

	@Override
	protected void doInitModule() {
		this.servletContext = ServletUtil.oldToNew(this.contextRoot, this.getServletContext());
		
		// TODO initialize non-negative load-on-startup Servlets
		// TODO keep track of uninitialized Servlets
		// TODO account for jsp-file in Servlet listings
		// TODO fire listeners
		// TODO setApplicationTimeoutMs and setSessionTimeoutMs
		// TODO look for ServletContainerInitializers in META-INF/services
	}
	
	@Override
	public void doService(String contextPath, String fullPath, HttpSessionAdapter httpSessionAdapter, HttpServletRequestAdapter servletRequest,
			HttpServletResponseAdapter servletResponse) throws javax.servlet.ServletException, IOException {
		String path = PathUtil.concat("/", StringUtil.toString(fullPath), '/'); //$NON-NLS-1$
		
		// TODO fire listeners
		// TODO match Servlets
		
		String resPath = path;
		int qIndex = resPath.indexOf('?');
		if(qIndex > -1) {
			resPath = resPath.substring(0, qIndex);
		}
		
		// Look in META-INF/resources
		String metaPath = PathUtil.concat("/META-INF/resources", resPath, '/'); //$NON-NLS-1$
		URL url = bundle.getResource(metaPath);
		if(url != null) {
			serveUrlResource(resPath, url, servletRequest, servletResponse);
			return;
		}
		
		
		// Look for a matching resource	in ContentLocation
		if(!path.startsWith("/WEB-INF/")) { //$NON-NLS-1$
			url = getResource(resPath);
			if(url != null) {
				serveUrlResource(resPath, url, servletRequest, servletResponse);
				return;
			}
		}
		
		handlePageNotFound(resPath, servletRequest, servletResponse);
	}

	@Override
	protected void doDestroyModule() {
		// TODO Auto-generated method stub

		// TODO fire listeners
	}
	
	@Override
	public void checkTimeout(long var1) {
		super.checkTimeout(var1);
		
		// TODO anything to do here? the super version handles session timeouts
	}
	
	@Override
	public EventListener[] createEventListeners() {
		// TODO Query listeners
		// This is used by the parent to find javax.servlet.ServletContextListeners to fire.
		// It'd probably make sense to include all app listeners as they are (to then reference
		//   in our own events) and also double up with wrappers for jakarta.* -> javax.* for
		//   the context listeners
		return super.createEventListeners();
	}
	
	@Override
	public javax.servlet.http.HttpSessionListener[] createSessionListeners() {
		// TODO Query listeners
		// Similar to above, this is used by the parent to fire session listeners as appropriate.
		// It'd probably make sense to put wrappers for any jakarta HttpSessionListeners here
		//   instead of in the eventListeners list
		return super.createSessionListeners();
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public ClassLoader getModuleClassLoader() {
		// TODO Implement to read from the bundle
		// TODO consider using BundleWebAppClassLoader
		return new ClassLoader() {
			@Override
			protected Class<?> findClass(String name) throws ClassNotFoundException {
				try {
					bundle.loadClass(name);
				} catch(ClassNotFoundException e) {
					// ignore and delegate up
				}
				return super.findClass(name);
			}
			
			@Override
			protected URL findResource(String name) {
				URL res = bundle.getResource(name);
				if(res != null) {
					return res;
				}
				return super.findResource(name);
			}
			
			@Override
			protected Enumeration<URL> findResources(String name) throws IOException {
				Enumeration<URL> res = bundle.getResources(name);
				if(res != null && res.hasMoreElements()) {
					return res;
				}
				return super.findResources(name);
			}
		};
	}

	@Override
	public URL getResource(String res) throws MalformedURLException {
		if(!contentLocation.isPresent()) {
			// Don't allow reading resources when there's no ContentLocation specified
			return null;
		}
		String loc = contentLocation.get();
		String path = PathUtil.concat(loc, res, '/');
		return bundle.getResource(path);
	}

	@Override
	public InputStream getResourceAsStream(String res) {
		try {
			URL url = getResource(res);
			if(url != null) {
				return url.openStream();
			} else {
				return null;
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public boolean getResourceAsStream(OutputStream os, String res) {
		try(InputStream is = getResourceAsStream(res)) {
			if(is != null) {
				StreamUtil.copyStream(is, os);
				return true;
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return false;
	}

	@Override
	public Set<String> getResourcePaths(String res) {
		if(!contentLocation.isPresent()) {
			// Don't allow reading resources when there's no ContentLocation specified
			return Collections.emptySet();
		}
		String path = PathUtil.concat(contentLocation.get(), res, '/');
		return new LinkedHashSet<>(Collections.list(bundle.getEntryPaths(path)));
	}

	@Override
	public boolean refresh() {
		// TODO consider if there's anything to do here
		return false;
	}

	@Override
	public boolean shouldRefresh() {
		// TODO consider monitoring for bundle events
		return false;
	}

	@Override
	public String toString() {
		return String.format("JakartaContainerModule [bundle=%s, contextRoot=%s, contentLocation=%s]", bundle,
				contextRoot, contentLocation);
	}
	
	// *******************************************************************************
	// * Internal implementation methods
	// *******************************************************************************

	private void serveUrlResource(String resPath, URL url, HttpServletRequestAdapter servletRequest, HttpServletResponseAdapter servletResponse) {
		try {
			// Look for a matching resource
			
			URLConnection conn = url.openConnection();
			
			try(InputStream is = conn.getInputStream()) {
				if(is != null) {
					
					long lastMod = conn.getLastModified();
					if(lastMod > 0) {
						// Check for cache
						long ifModifiedSince = servletRequest.getDateHeader(HttpHeaders.IF_MODIFIED_SINCE);
						if(ifModifiedSince > 0 && ifModifiedSince >= lastMod) {
							servletResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
							return;
						}
					}
					servletResponse.addDateHeader(HttpHeaders.LAST_MODIFIED, lastMod);
					
					String mimeType = LibraryUtil.detectMimeType(resPath);
					
					servletResponse.setStatus(HttpServletResponse.SC_OK);
					servletResponse.setContentType(mimeType);
					int contentLength = conn.getContentLength();
					servletResponse.setContentLength(contentLength);
					
					try(javax.servlet.ServletOutputStream os = servletResponse.getOutputStream()) {
						StreamUtil.copyStream(is, os);
					}
					
					return;
				}
			}
		} catch(FileNotFoundException e) {
			handlePageNotFound(resPath, servletRequest, servletResponse);
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}
	
	private void handlePageNotFound(String path, HttpServletRequestAdapter servletRequest, HttpServletResponseAdapter servletResponse) {
		// TODO consider handling different Accept headers
		servletResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
		
		// TODO try to glean from server locale
		String lang = null;
		boolean rtl = false;
		try(PrintWriter w = servletResponse.getWriter()) {
			XSPErrorPage.handlePageNotFound(w, path, null, lang, rtl);
		} catch(IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
