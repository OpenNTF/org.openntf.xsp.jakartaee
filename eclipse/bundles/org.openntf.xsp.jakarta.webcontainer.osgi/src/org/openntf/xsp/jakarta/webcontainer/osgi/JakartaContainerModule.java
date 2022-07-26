package org.openntf.xsp.jakarta.webcontainer.osgi;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.EventListener;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.osgi.framework.Bundle;

import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.StreamUtil;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;
import com.ibm.designer.runtime.domino.adapter.util.PageNotFoundException;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletRequestAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletResponseAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpSessionAdapter;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Jesse Gallagher
 * @since 2.8.0
 */
public class JakartaContainerModule extends ComponentModule {
	
	private final Bundle bundle;
	private final String contextRoot;
	private final Optional<String> contentLocation;

	public JakartaContainerModule(LCDEnvironment env, JakartaContainerService service, Bundle bundle, String contextRoot, String contentLocation) {
		super(env, service, JakartaContainerModule.class.getSimpleName() + ": " + contextRoot, true); //$NON-NLS-1$
		this.bundle = bundle;
		this.contextRoot = contextRoot;
		if(StringUtil.isNotEmpty(contentLocation)) {
			this.contentLocation = Optional.of(contentLocation);
		} else {
			this.contentLocation = Optional.empty();
		}
	}

	@Override
	protected void doInitModule() {
		// TODO Auto-generated method stub

		// TODO create a new ServletContext
		// TODO initialize non-negative load-on-startup Servlets
		// TODO keep track of uninitialized Servlets
		// TODO fire listeners
		// - use createServlet and createServletConfig from LCDEnvironment?
		// TODO setApplicationTimeoutMs and setSessionTimeoutMs
	}
	
	@Override
	public void doService(String contextPath, String fullPath, HttpSessionAdapter httpSessionAdapter, HttpServletRequestAdapter servletRequest,
			HttpServletResponseAdapter servletResponse) throws javax.servlet.ServletException, IOException {
		String path = StringUtil.toString(fullPath);
		
		// TODO fire listeners
		// TODO match Servlets
		
		try {
			// Look for a matching resource
			int qIndex = path.indexOf('?');
			if(qIndex > -1) {
				path = path.substring(0, qIndex);
			}
			URL url = getResource(path);
			if(url != null) {
				try(InputStream is = url.openStream()) {
					if(is != null) {
						// TODO guess type
						servletResponse.setStatus(HttpServletResponse.SC_OK);
						servletResponse.setContentType("text/plain"); //$NON-NLS-1$
						try(javax.servlet.ServletOutputStream os = servletResponse.getOutputStream()) {
							StreamUtil.copyStream(is, os);
						}
						
						return;
					}
				}
			}
		} catch(FileNotFoundException e) {
			throw new PageNotFoundException(fullPath);
		} catch(Throwable t) {
			t.printStackTrace();
		}
		
		throw new PageNotFoundException(fullPath);
		
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
		return new ClassLoader() {
			
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

}
