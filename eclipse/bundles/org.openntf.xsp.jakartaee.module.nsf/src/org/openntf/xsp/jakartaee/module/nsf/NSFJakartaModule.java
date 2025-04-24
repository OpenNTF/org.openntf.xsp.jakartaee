package org.openntf.xsp.jakartaee.module.nsf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ibm.commons.extension.ExtensionManager;
import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesCollectionEntry;
import com.ibm.designer.domino.napi.NotesConstants;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.designer.domino.napi.NotesNote;
import com.ibm.designer.domino.napi.NotesSession;
import com.ibm.designer.domino.napi.design.FileAccess;
import com.ibm.designer.domino.napi.util.NotesUtils;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.IServletFactory;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;
import com.ibm.designer.runtime.domino.adapter.ServletMatch;
import com.ibm.designer.runtime.domino.adapter.servlet.LCDAdapterHttpSession;
import com.ibm.designer.runtime.domino.adapter.util.PageNotFoundException;
import com.ibm.designer.runtime.domino.bootstrap.adapter.DominoHttpXspNativeContext;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletRequestAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletResponseAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpSessionAdapter;
import com.ibm.domino.napi.NException;
import com.ibm.domino.napi.c.NotesUtil;
import com.ibm.domino.napi.c.xsp.XSPNative;
import com.ibm.xsp.acl.NoAccessSignal;

import org.openntf.xsp.jakarta.cdi.bean.HttpContextBean;
import org.openntf.xsp.jakarta.cdi.util.ContainerUtil;
import org.openntf.xsp.jakartaee.module.JakartaIServletFactory;
import org.openntf.xsp.jakartaee.module.nsf.io.DesignCollectionIterator;
import org.openntf.xsp.jakartaee.module.nsf.io.NSFAccess;
import org.openntf.xsp.jakartaee.module.nsf.util.ActiveRequest;
import org.openntf.xsp.jakartaee.module.nsf.util.LSXBEHolder;
import org.openntf.xsp.jakartaee.module.nsf.util.ModuleMap;
import org.openntf.xsp.jakartaee.module.nsf.util.UncheckedNotesException;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.jakartaee.util.ModuleUtil;
import org.openntf.xsp.jakartaee.util.PriorityComparator;

import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HandlesTypes;
import jakarta.servlet.http.HttpServletRequest;
import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.NotesThread;
import lotus.domino.Session;

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
	private Collection<JakartaIServletFactory> servletFactories;
	private boolean initialized;
	private NotesSession notesSession;
	private NotesDatabase notesDatabase;
	private NSFJakartaModuleClassLoader moduleClassLoader;
	private long lastRefresh;
	private String xspSigner;
	
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
	
	public NotesSession getNotesSession() {
		return notesSession;
	}
	
	public NotesDatabase getNotesDatabase() {
		return notesDatabase;
	}

	@Override
	protected void doInitModule() {
		NotesThread.sinitThread();
		try {
			try {
				if(this.moduleClassLoader != null) {
					this.moduleClassLoader.close();
				}
				if(this.notesSession != null) {
					this.notesSession.recycle();
				}
				
				this.notesSession = new NotesSession();
				this.notesDatabase = notesSession.getDatabase(this.mapping.nsfPath());
				this.notesDatabase.open();
				
				// Use xsp.properties as our signer if available
				NotesNote xspProperties = FileAccess.getFileByPath(this.notesDatabase, "WEB-INF/xsp.properties"); //$NON-NLS-1$
				if(xspProperties != null) {
					List<String> updatedBy = xspProperties.getItemAsTextList(NotesConstants.FIELD_UPDATED_BY);
					if(!updatedBy.isEmpty()) {
						this.xspSigner = updatedBy.getLast();
					} else {
						this.xspSigner = this.notesDatabase.getUserName();
					}
					xspProperties.recycle();
				} else {
					this.xspSigner = this.notesDatabase.getUserName();
				}
				
				this.moduleClassLoader = new NSFJakartaModuleClassLoader(this);
				
				this.servletFactories = ExtensionManager.findApplicationServices(getModuleClassLoader(), "com.ibm.xsp.adapter.servletFactory").stream() //$NON-NLS-1$
					.filter(JakartaIServletFactory.class::isInstance)
					.map(JakartaIServletFactory.class::cast)
					.sorted(PriorityComparator.DESCENDING)
					.toList();
				this.servletFactories.forEach(fac -> fac.init(this));
			} catch (NotesAPIException e) {
				throw new RuntimeException(MessageFormat.format("Encountered exception initializing module {0}", this), e);
			}
			
			// Fire ServletContainerInitializers
			ServletContext servletContext = ServletUtil.oldToNew('/' + mapping.path(), getServletContext());
			List<ServletContainerInitializer> initializers = LibraryUtil.findExtensions(ServletContainerInitializer.class, this);
			for(ServletContainerInitializer initializer : initializers) {
				Set<Class<?>> classes = null;
				if(initializer.getClass().isAnnotationPresent(HandlesTypes.class)) {
					classes = ModuleUtil.buildMatchingClasses(initializer.getClass().getAnnotation(HandlesTypes.class), this);
				}
				try {
					initializer.onStartup(classes, servletContext);
				} catch (ServletException e) {
					throw new RuntimeException(e);
				}
			}
			
			// CDI gets initialized immediately
			ContainerUtil.getContainer(this);
			
			this.initialized = true;
		} finally {
			NotesThread.stermThread();
		}
	}
	
	public Collection<? extends IServletFactory> getServletFactories() {
		return servletFactories;
	}
	
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	protected void doDestroyModule() {
		this.initialized = false;
		
		if(ContainerUtil.getContainer(this) instanceof AutoCloseable cdi) {
			try {
				cdi.close();
			} catch(Exception e) {
				if(log.isLoggable(Level.WARNING)) {
					log.log(Level.WARNING, MessageFormat.format("Encountered exception closing CDI container for {0}", this), e);
				}
			}
		}
		
		if(this.moduleClassLoader != null) {
			this.moduleClassLoader.close();
			this.moduleClassLoader = null;
		}
		
		try {
			this.notesSession.recycle();
			this.notesSession = null;
			this.notesDatabase = null;
		} catch (NotesAPIException e) {
			// Ignore
		}
	}
	
	@Override
	public void doService(String contextPath, String pathInfo, HttpSessionAdapter httpSessionAdapter, HttpServletRequestAdapter servletRequest,
			HttpServletResponseAdapter servletResponse) throws javax.servlet.ServletException, IOException {
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
	public NSFJakartaModuleClassLoader getModuleClassLoader() {
		return this.moduleClassLoader;
	}

	@Override
	public URL getResource(String res) throws MalformedURLException {
		return NSFAccess.getUrl(this.mapping.nsfPath(), trimResourcePath(res))
			.orElseGet(() -> {
				// Check for META-INF/resources in embedded JARs
				String metaResPath = PathUtil.concat("META-INF/resources", res, '/'); //$NON-NLS-1$
				return this.moduleClassLoader.getJarResource(metaResPath);
			});
	}

	@Override
	public InputStream getResourceAsStream(String res) {
		try {
			InputStream is = NSFAccess.openStream(this.mapping.nsfPath(), trimResourcePath(res));
			if(is != null) {
				return is;
			}
			
			// Check for META-INF/resources in embedded JARs
			String metaResPath = PathUtil.concat("META-INF/resources", res, '/'); //$NON-NLS-1$
			return this.moduleClassLoader.getJarResourceAsStream(metaResPath);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public Set<String> getResourcePaths(String res) {
		// This is looking for all resources strictly within the folder path,
		//   with a trailing "/" if it's a subfolder of it
		// TODO look for subfolders?
		Stream<String> matches = this.listFiles(res);
		if(res.charAt(0) == '/') {
			matches = matches.map(p -> '/' + p);
		}
		return matches.collect(Collectors.toSet());
	}

	@Override
	public boolean refresh() {
		if(log.isLoggable(Level.FINE)) {
			log.fine(MessageFormat.format("Refreshing module {0}", this));
		}
		doDestroyModule();
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
		try {
			long lastRefresh = getLastRefresh();
			long designMod = this.notesDatabase.getLastNonDataModificationDate() * 1000;
			// Fuzz by a second to account for lack of precision in designMod
			long diff = designMod - lastRefresh;
			return diff > 1000;
		} catch(NotesAPIException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public boolean isExpired(long paramLong) {
		return super.isExpired(paramLong);
	}
	
	// These are called by AdapterInvoker
	
	@Override
	public ServletMatch getServlet(String path) throws javax.servlet.ServletException {
		for(JakartaIServletFactory fac : this.servletFactories) {
			ServletMatch servletMatch = fac.getServletMatch('/' + mapping.path(), path);
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
	protected void invokeServlet(javax.servlet.Servlet servlet, javax.servlet.http.HttpServletRequest req,
			javax.servlet.http.HttpServletResponse resp) throws javax.servlet.ServletException, IOException {
		if(log.isLoggable(Level.FINE)) {
			log.fine(MessageFormat.format("Invoking Servlet {0}", servlet));
		}

		HttpContextBean.setThreadResponse(ServletUtil.oldToNew(resp));
		// Update the active request with the "true" request object
		HttpServletRequest request = ServletUtil.oldToNew(getServletContext(), req);
		ActiveRequest.pushRequest(request);
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
	
	@Override
	public void addSession(String id, LCDAdapterHttpSession session) {
		// TODO Init CDI session
		super.addSession(id, session);
	}
	
	@Override
	public void removeSession(String id) {
		// TODO Term CDI session
		super.removeSession(id);
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
		return this.shouldRefresh();
	}
	
	// Called when isResourcesCache() == true
	@Override
	public long getResourcesExpireTime(String res) {
		// TODO look for xsp.expires app property
		// TODO check for image types like normal NSFComponentModule
		return 864000000L;
	}
	
	@Override
	protected void writeResource(ServletInvoker invoker, String res) throws IOException {
		// Do an early check here since otherwise the parent implementation will set a status of 200
		if(getResource(res) == null) {
			// TODO consider handling this differently, to avoid just "Item Not Found Exception" and a console log entry
			throw new PageNotFoundException(MessageFormat.format("No resource found at path {0}", res));
		} else {
			super.writeResource(invoker, res);
		}
	}

	// Called to serve resource - returns false if it doesn't exist
	@Override
	public boolean getResourceAsStream(OutputStream os, String res) {
		try(InputStream is = getResourceAsStream(res)) {
			if(is != null) {
				is.transferTo(os);
				return true;
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return false;
	}
	
	public Stream<String> listFiles(String basePath) {
		String path = trimResourcePath(basePath);
		boolean listAll = StringUtil.isEmpty(path);
		if(!listAll && !path.endsWith("/")) { //$NON-NLS-1$
			path += "/"; //$NON-NLS-1$
		}
		
		// TODO cache this list and destroy on refresh
		List<String> result = new ArrayList<>();
		try (DesignCollectionIterator nav = new DesignCollectionIterator(notesDatabase)) {
			while (nav.hasNext()) {
				NotesCollectionEntry entry = nav.next();

				String flags = entry.getItemValueAsString(NotesConstants.DESIGN_FLAGS);
				if(NotesUtils.CmemflagTestMultiple(flags, NotesConstants.DFLAGPAT_FILE)) {
					// In practice, we don't care about $ClassIndexItem
					String name = entry.getItemValueAsString(NotesConstants.FIELD_TITLE);
					if(NotesUtils.CmemflagTestMultiple(flags, NotesConstants.DFLAGPAT_JAVAJAR)) {
						name = "WEB-INF/lib/" + name; //$NON-NLS-1$
					}
					result.add(name);
				}

				entry.recycle();
			}
		} catch (NotesAPIException e) {
			throw new RuntimeException(e);
		}

		Stream<String> pathStream = result.stream();
		if(!listAll) {
			String fPath = path;
			pathStream = pathStream
				.filter(p -> p.startsWith(fPath) && p.indexOf('/', fPath.length()+1) == -1);
		}
		return pathStream;
	}
	
	@Override
	public String toString() {
		return MessageFormat.format("{0}: {1}", getClass().getSimpleName(), mapping);
	}
	
	public LSXBEHolder withSessions(HttpServletRequestAdapter req) {
		if(req instanceof DominoHttpXspNativeContext nativeCtx) {
			try {
				Principal principal = req.getUserPrincipal();
				String name = principal == null ? "Anonymous" : principal.getName(); //$NON-NLS-1$
				Session session = XSPNative.createXPageSession(name, nativeCtx.getUserListHandle(), nativeCtx.getEnforceAccess(), nativeCtx.getPreviewServer());
				Database database = session.getDatabase("", this.mapping.nsfPath()); //$NON-NLS-1$
				XSPNative.setContextDatabase(session, XSPNative.getDBHandle(database));
				
				// Use the signer of xsp.properties - failing that, the server
				long hSigner = NotesUtil.createUserNameList(this.xspSigner);
				Session sessionAsSigner = XSPNative.createXPageSessionExt(this.xspSigner, hSigner, false, nativeCtx.getPreviewServer(), false);
				Session sessAsSignerFullAccess = XSPNative.createXPageSessionExt(this.xspSigner, hSigner, false, nativeCtx.getPreviewServer(), false);
				
				return new LSXBEHolder(session, database, sessionAsSigner, sessAsSignerFullAccess, hSigner);
			} catch(NException e) {
				throw new RuntimeException(e);
			} catch(NotesException e) {
				if(e.id == 0x0FDC) {
					// User X cannot open database
					NoAccessSignal signal = new NoAccessSignal(e.text);
					signal.setStackTrace(new StackTraceElement[0]);
					throw signal;
				}
				throw new UncheckedNotesException(e);
			}
		} else {
			throw new IllegalArgumentException(MessageFormat.format("Request must be an instance of DominoHttpXspNativeContext: {0}", req));
		}
	}
	
	private String trimResourcePath(String path) {
		if(path == null) {
			return null;
		} else if(path.length() > 1 && path.startsWith("/")) { //$NON-NLS-1$
			return path.substring(1);
		} else {
			return path;
		}
	}
}
