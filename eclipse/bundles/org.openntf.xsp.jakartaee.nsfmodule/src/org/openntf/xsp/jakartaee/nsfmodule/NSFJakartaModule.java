package org.openntf.xsp.jakartaee.nsfmodule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;
import com.ibm.designer.runtime.domino.adapter.ServletMatch;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletRequestAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletResponseAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpSessionAdapter;
import com.ibm.domino.xsp.module.nsf.NSFComponentModule;
import com.ibm.domino.xsp.module.nsf.NSFService;
import com.ibm.domino.xsp.module.nsf.NotesContext;
import com.ibm.domino.xsp.module.nsf.RuntimeFileSystem;

/**
 * @since 3.4.0
 */
public class NSFJakartaModule extends ComponentModule {
	private final ModuleMap mapping;
	private NSFComponentModule delegate;
	private boolean initialized;
	
	public NSFJakartaModule(LCDEnvironment env, NSFJakartaModuleService service, ModuleMap mapping) {
		super(env, service, MessageFormat.format("{0} -> {1}", mapping.path(), mapping.nsfPath()), true);
		this.mapping = mapping;
	}
	
	@Override
	public NSFJakartaModuleService getHttpService() {
		return (NSFJakartaModuleService)super.getHttpService();
	}
	
	public ModuleMap getMapping() {
		return mapping;
	}
	
	public NSFComponentModule getDelegate() {
		return this.delegate;
	}

	@Override
	protected void doInitModule() {
		NSFService nsfService = getHttpService().getNSFService();
		try {
			NSFComponentModule delegate = nsfService.loadModule(mapping.nsfPath());
			this.delegate = Objects.requireNonNull(delegate, MessageFormat.format("Unable to open database {0}", mapping.nsfPath()));
		} catch(ServletException e) {
			e.printStackTrace();
			throw new RuntimeException(MessageFormat.format("Encountered exception loading database {0}", mapping.nsfPath()), e);
		}
		
		this.initialized = true;
	}
	
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	protected void doDestroyModule() {
		this.initialized = false;
	}
	
	@Override
	public void doService(String contextPath, String pathInfo, HttpSessionAdapter httpSessionAdapter, HttpServletRequestAdapter servletRequest,
			HttpServletResponseAdapter servletResponse) throws ServletException, IOException {
		System.out.println(getClass().getSimpleName() + "#doService with contextPath=" + contextPath + ", pathInfo=" + pathInfo);
		NotesContext notesContext = new NotesContext(delegate);
		NotesContext.initThread(notesContext);
		try {
			super.doService(contextPath, pathInfo, httpSessionAdapter, servletRequest, servletResponse);
		} finally {
			NotesContext.termThread();
		}
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public ClassLoader getModuleClassLoader() {
		return delegate.getModuleClassLoader();
	}

	@Override
	public URL getResource(String res) throws MalformedURLException {
		return delegate.getResource(res);
	}

	@Override
	public InputStream getResourceAsStream(String res) {
		return delegate.getResourceAsStream(res);
	}

	@Override
	public Set<String> getResourcePaths(String res) {
		return delegate.getResourcePaths(res);
	}

	@Override
	public boolean refresh() {
		return delegate.refresh();
	}

	@Override
	public boolean shouldRefresh() {
		return delegate.shouldRefresh();
	}
	
	public RuntimeFileSystem getRuntimeFileSystem() {
		return delegate.getRuntimeFileSystem();
	}
	
	// These are called by AdapterInvoker
	
	@Override
	public ServletMatch getServlet(String paramString) throws ServletException {
		// TODO Auto-generated method stub
		System.out.println(mapping + " - getServlet for " + paramString);
		return super.getServlet(paramString);
	}
	
	// Called if getServlet returns null
	// We return false here because we don't want to interfere with
	//   existing third-party libraries that assume NSFComponentModule
	@Override
	public boolean hasServletFactories() {
		return false;
	}
	
	// Called by AdapterInvoker if getServlet or a ServletFactory returns a ServletMatch
	@Override
	protected void invokeServlet(Servlet paramServlet, HttpServletRequest paramHttpServletRequest,
			HttpServletResponse paramHttpServletResponse) throws ServletException, IOException {
		// TODO Auto-generated method stub
		super.invokeServlet(paramServlet, paramHttpServletRequest, paramHttpServletResponse);
	}
	
	// Called if there's no ServletMatch
	@Override
	public boolean hasBundleResource() {
		return delegate.hasBundleResource();
	}
	
	// Called by writeResource to see if it should cache resources
	@Override
	public boolean isResourcesCache() {
		return delegate.isResourcesCache();
	}
	
	// Called when isResourcesCache() == true
	@Override
	public boolean isResourcesModifiedSince(String res, long t) {
		System.out.println("asking for isResourcesModifiedSince " + res);
		return delegate.isResourcesModifiedSince(res, t);
	}
	
	// Called when isResourcesCache() == true
	@Override
	public long getResourcesExpireTime(String res) {
		return delegate.getResourcesExpireTime(res);
	}

	// Called to serve resource - returns false if it doesn't exist
	@Override
	public boolean getResourceAsStream(OutputStream os, String res) {
		System.out.println("asking for resourceAsStream " + res);
		return delegate.getResourceAsStream(os, res);
	}

}
