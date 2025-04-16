package org.openntf.xsp.jakartaee.nsfmodule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.hcl.domino.module.nsf.NSFComponentModule;
import com.hcl.domino.module.nsf.NotesContext;
import com.hcl.domino.module.nsf.RuntimeFileSystem;
import com.hcl.domino.module.nsf.RuntimeFileSystem.NSFFile;
import com.hcl.domino.module.nsf.RuntimeFileSystem.NSFFolder;
import com.hcl.domino.module.nsf.RuntimeFileSystem.NSFResource;
import com.ibm.commons.extension.ExtensionManager;
import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;
import com.ibm.designer.runtime.domino.adapter.ServletMatch;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletRequestAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletResponseAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpSessionAdapter;

import org.openntf.xsp.jakarta.cdi.bean.HttpContextBean;
import org.openntf.xsp.jakartaee.module.JakartaIServletFactory;
import org.openntf.xsp.jakartaee.nsfmodule.io.NSFJakartaURL;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;

/**
 * @since 3.4.0
 */
public class NSFJakartaModule extends ComponentModule {
	private static final Logger log = Logger.getLogger(NSFJakartaModule.class.getPackageName());
	
	static {
		// TODO switch back
		log.setLevel(Level.FINEST);
	}
	
	private final ModuleMap mapping;
	private NSFComponentModule delegate;
	private Collection<JakartaIServletFactory> servletFactories;
	private boolean initialized;
	private NSFJakartaModuleClassLoader moduleClassLoader;
	private long lastRefresh;
	
	public NSFJakartaModule(LCDEnvironment env, NSFJakartaModuleService service, ModuleMap mapping) {
		super(env, service, MessageFormat.format("{0} - {1}", mapping.path(), mapping.nsfPath()), true);
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
		this.delegate = new NSFComponentModule(mapping.nsfPath());

		try(WithContext c = withContext()) {
			this.delegate.initModule();
			
			if(this.moduleClassLoader != null) {
				this.moduleClassLoader.close();
			}
			this.moduleClassLoader = new NSFJakartaModuleClassLoader(this);
			
			this.servletFactories = ExtensionManager.findApplicationServices(getModuleClassLoader(), "com.ibm.xsp.adapter.servletFactory").stream() //$NON-NLS-1$
				.filter(JakartaIServletFactory.class::isInstance)
				.map(JakartaIServletFactory.class::cast)
				.peek(fac -> fac.init(this))
				.toList();
		}
		
		this.initialized = true;
	}
	
	
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	protected void doDestroyModule() {
		this.initialized = false;
		
		if(this.moduleClassLoader != null) {
			this.moduleClassLoader.close();
			this.moduleClassLoader = null;
		}
	}
	
	@Override
	public void doService(String contextPath, String pathInfo, HttpSessionAdapter httpSessionAdapter, HttpServletRequestAdapter servletRequest,
			HttpServletResponseAdapter servletResponse) throws ServletException, IOException {
		if(log.isLoggable(Level.FINER)) {
			log.finer(getClass().getSimpleName() + "#doService with contextPath=" + contextPath + ", pathInfo=" + pathInfo);
		}
		
		super.doService(contextPath, pathInfo, httpSessionAdapter, servletRequest, servletResponse);
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public ClassLoader getModuleClassLoader() {
		return this.moduleClassLoader;
	}

	@Override
	public URL getResource(String res) throws MalformedURLException {
		try(WithContext ctx = withContext()) {
			RuntimeFileSystem fs = delegate.getRuntimeFileSystem();
			if(fs.exists(res)) {
				return NSFJakartaURL.of(this.mapping.nsfPath(), res);
			}
		}
		return null;
	}

	@Override
	public InputStream getResourceAsStream(String res) {
		try(WithContext ctx = withContext()) {
			RuntimeFileSystem fs = delegate.getRuntimeFileSystem();
			// TODO check if the paths have the same symantics
			NSFResource nsfRes = fs.getResource(res);
			if(nsfRes instanceof NSFFile file) {
				return fs.getFileContent(NotesContext.getCurrent().getNotesDatabase(), file);
			}
		} catch (NotesAPIException | IOException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

	@Override
	public Set<String> getResourcePaths(String res) {
		// This is looking for all resources strictly within the folder path,
		//   with a trailing "/" if it's a subfolder of it
		Set<String> result = new HashSet<>();
		for(Map.Entry<String, NSFResource> entry : delegate.getRuntimeFileSystem().getAllResources().entrySet()) {
			String path = entry.getKey();
			int slashIndex = path.lastIndexOf('/');
			if(slashIndex > -1) {
				String parentPath = path.substring(0, slashIndex);
				if(StringUtil.equals(res, parentPath)) {
					NSFResource nsfRes = entry.getValue();
					if(nsfRes instanceof NSFFolder) {
						result.add(path + '/');
					} else {
						result.add(path);
					}
				}
			}
		}
		return result;
	}

	@Override
	public boolean refresh() {
		if(log.isLoggable(Level.FINE)) {
			log.fine(MessageFormat.format("Refreshing module {0}", this));
		}
		doInitModule();
		this.lastRefresh = System.currentTimeMillis();
		return true;
	}
	
	@Override
	public long getLastRefresh() {
		return Math.max(lastRefresh, super.getLastRefresh());
	}

	@Override
	public boolean shouldRefresh() {
		try(WithContext w = withContext()) {
			boolean shouldRefresh = delegate.shouldRefresh();
			if(log.isLoggable(Level.FINEST)) {
				log.finest(MessageFormat.format("{0} - should refresh? {1}", this, shouldRefresh));
			}
			return shouldRefresh;
		}
	}
	
	@Override
	public boolean isExpired(long paramLong) {
		return super.isExpired(paramLong);
	}
	
	public RuntimeFileSystem getRuntimeFileSystem() {
		return delegate.getRuntimeFileSystem();
	}
	
	// These are called by AdapterInvoker
	
	@Override
	public ServletMatch getServlet(String path) throws ServletException {
		for(JakartaIServletFactory fac : this.servletFactories) {
			ServletMatch servletMatch = fac.getServletMatch("", path); //$NON-NLS-1$
			if(servletMatch != null) {
				return servletMatch;
			}
		}
		return null;
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
	protected void invokeServlet(Servlet servlet, HttpServletRequest req,
			HttpServletResponse resp) throws ServletException, IOException {
		if(log.isLoggable(Level.FINE)) {
			log.fine(MessageFormat.format("Invoking Servlet {0}", servlet));
		}
		HttpContextBean.setThreadResponse(ServletUtil.oldToNew(resp));;
		try {
			super.invokeServlet(servlet, req, resp);
		} catch(Throwable t) {
			if(log.isLoggable(Level.WARNING)) {
				log.log(Level.WARNING, MessageFormat.format("Encountered exception invoking Servlet {0} in {1}", servlet, this), t);
			}
			throw t;
		} finally {
			HttpContextBean.setThreadResponse(null);
		}
	}
	
	// Called if there's no ServletMatch
	@Override
	public boolean hasBundleResource() {
		return true;
	}
	
	// Called by writeResource to see if it should cache resources
	@Override
	public boolean isResourcesCache() {
		return true;
	}
	
	// Called when isResourcesCache() == true
	@Override
	public boolean isResourcesModifiedSince(String res, long t) {
		// TODO consider making this fine-grained, though it's probably speedy as-is
		return delegate.getRuntimeFileSystem().getLastModificationDate() > t;
	}
	
	// Called when isResourcesCache() == true
	@Override
	public long getResourcesExpireTime(String res) {
		// TODO look for xsp.expires app property
		// TODO check for image types like normal NSFComponentModule
		return 864000000L;
	}

	// Called to serve resource - returns false if it doesn't exist
	@Override
	public boolean getResourceAsStream(OutputStream os, String res) {
		try(WithContext ctx = withContext()) {
			try(InputStream is = getResourceAsStream(res)) {
				if(is != null) {
					is.transferTo(os);
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			return false;
		}
	}
	
	@Override
	public String toString() {
		return MessageFormat.format("{0}: {1}", getClass().getSimpleName(), mapping);
	}
	
	public WithContext withContext() {
		if(NotesContext.contextThreadLocal.get() == null) {
			NotesContext notesContext = new NotesContext(delegate);
			NotesContext.initThread(notesContext);
			return new WithContextImpl(notesContext);
		} else {
			return new WithContextNop();
		}
	}
	
	public interface WithContext extends AutoCloseable {
		void close();
	}

	public record WithContextImpl(NotesContext notesContext) implements WithContext {
		@Override
		public void close() {
			NotesContext.termThread();
		}
	}
	
	public record WithContextNop() implements WithContext {
		@Override
		public void close() {
		}
	}
}
