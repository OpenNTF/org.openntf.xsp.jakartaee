/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.xsp.jakartaee.module.jakartansf;

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
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ibm.commons.extension.ExtensionManager;
import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesConstants;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.designer.domino.napi.NotesNote;
import com.ibm.designer.domino.napi.NotesSession;
import com.ibm.designer.domino.napi.design.FileAccess;
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
import com.ibm.domino.napi.c.BackendBridge;
import com.ibm.domino.napi.c.NotesUtil;
import com.ibm.domino.napi.c.xsp.XSPNative;
import com.ibm.xsp.acl.NoAccessSignal;

import org.apache.tomcat.util.descriptor.web.WebXml;
import org.openntf.xsp.jakarta.cdi.bean.HttpContextBean;
import org.openntf.xsp.jakarta.cdi.util.ContainerUtil;
import org.openntf.xsp.jakartaee.module.JakartaIServletFactory;
import org.openntf.xsp.jakartaee.module.ServletContainerInitializerProvider;
import org.openntf.xsp.jakartaee.module.jakartansf.concurrency.NSFJakartaModuleConcurrencyListener;
import org.openntf.xsp.jakartaee.module.jakartansf.io.NSFJakartaFileSystem;
import org.openntf.xsp.jakartaee.module.jakartansf.util.ActiveRequest;
import org.openntf.xsp.jakartaee.module.jakartansf.util.LSXBEHolder;
import org.openntf.xsp.jakartaee.module.jakartansf.util.ModuleMap;
import org.openntf.xsp.jakartaee.module.jakartansf.util.NSFModuleUtil;
import org.openntf.xsp.jakartaee.module.jakartansf.util.UncheckedNotesException;
import org.openntf.xsp.jakartaee.module.jakartansf.util.WelcomePageRequestAdapter;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.jakartaee.util.ModuleUtil;
import org.openntf.xsp.jakartaee.util.PriorityComparator;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.annotation.HandlesTypes;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.NotesThread;
import lotus.domino.Session;

/**
 * @since 3.4.0
 */
public class NSFJakartaModule extends ComponentModule {
	private static final Logger log = Logger.getLogger(NSFJakartaModule.class.getPackageName());
	
	private final ModuleMap mapping;
	private Collection<JakartaIServletFactory> servletFactories;
	private boolean initialized;
	private NotesSession notesSession;
	private NotesDatabase notesDatabase;
	private NSFJakartaModuleClassLoader moduleClassLoader;
	private NSFJakartaFileSystem fileSystem;
	private long lastRefresh;
	private String xspSigner;
	private ServletContext servletContext;
	
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

	@SuppressWarnings("unchecked")
	@Override
	protected void doInitModule() {
		// Clear all attributes except temp dir
		Map<String, Object> attrs = this.getAttributes();
		Object tempDir = attrs.get("javax.servlet.context.tempdir"); //$NON-NLS-1$
		attrs.clear();
		attrs.put("javax.servlet.context.tempdir", tempDir); //$NON-NLS-1$
		attrs.put("jakarta.servlet.context.tempdir", tempDir); //$NON-NLS-1$
		
		this.servletContext = ServletUtil.oldToNew('/' + this.mapping.path(), getServletContext());
		
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
						this.xspSigner = updatedBy.get(updatedBy.size()-1);
					} else {
						this.xspSigner = this.notesDatabase.getUserName();
					}
					xspProperties.recycle();
				} else {
					this.xspSigner = this.notesDatabase.getUserName();
				}

				this.fileSystem = new NSFJakartaFileSystem(this);
				this.moduleClassLoader = new NSFJakartaModuleClassLoader(this);
			} catch (NotesAPIException e) {
				throw new RuntimeException(MessageFormat.format("Encountered exception initializing module {0}", this), e);
			}

			// Register Servlet factories early, since initial
			this.servletFactories = ExtensionManager.findApplicationServices(getModuleClassLoader(), "com.ibm.xsp.adapter.servletFactory").stream() //$NON-NLS-1$
				.filter(JakartaIServletFactory.class::isInstance)
				.map(JakartaIServletFactory.class::cast)
				.sorted(PriorityComparator.DESCENDING)
				.toList();
			
			// Set lastRefresh, as used by ContainerUtil, before initializing CDI
			this.lastRefresh = System.currentTimeMillis();

			// Find ServletContainerInitializers
			List<ServletContainerInitializer> initializers = new ArrayList<>(LibraryUtil.findExtensions(ServletContainerInitializer.class, this));
			LibraryUtil.findExtensions(ServletContainerInitializerProvider.class).stream()
				.map(p -> p.provide(this))
				.filter(Objects::nonNull)
				.forEach(initializers::addAll);
			
			// Find any declared or annotated listeners
			WebXml webXml = ServletUtil.getWebXml(this);
			for(String listenerClassName : webXml.getListeners()) {
				try {
					Class<? extends EventListener> c = (Class<? extends EventListener>) Class.forName(listenerClassName, true, this.moduleClassLoader);
					servletContext.addListener(c);
				} catch (ClassNotFoundException e) {
					if(log.isLoggable(Level.WARNING)) {
						log.log(Level.WARNING, MessageFormat.format("Encountered exception loading listener class \"{0}\"", listenerClassName), e);
					}
				}
			}
			
			ModuleUtil.getClasses(this)
				.filter(c -> c.isAnnotationPresent(WebListener.class))
				.map(c -> (Class<? extends EventListener>)c)
				.forEach(servletContext::addListener);
			
			// Built-in support for concurrency
			servletContext.addListener(NSFJakartaModuleConcurrencyListener.class);
			
			ServletUtil.populateWebXmlParams(this, servletContext);
			
			try(
				var withCl = new WithClassLoader();
				var lsxbe = this.withSession(this.xspSigner);
			) {
				ActiveRequest.set(new ActiveRequest(this, lsxbe, null));
				
				// Initialize CDI early
				CDI<Object> cdi = ContainerUtil.getContainer(this);
				servletContext.setAttribute(BeanManager.class.getName(), ContainerUtil.getBeanManager(cdi));
				
				// Run through any container initializers
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
				
				ServletUtil.contextInitialized(servletContext);
				
				this.servletFactories.forEach(fac -> fac.init(this));
			} finally {
				ActiveRequest.set(null);
			}
			
			this.initialized = true;
		} finally {
			NotesThread.stermThread();
		}
	}
	
	public ServletContext getJakartaServletContext() {
		return servletContext;
	}
	
	public Collection<? extends IServletFactory> getServletFactories() {
		return servletFactories;
	}
	
	public String getXspSigner() {
		return xspSigner;
	}
	
	public boolean isInitialized() {
		return initialized;
	}
	
	@Override
	public String getWelcomePage() {
		Set<String> files = ServletUtil.getWebXml(this).getWelcomeFiles();
		if(!files.isEmpty()) {
			try(var withCl = new WithClassLoader()) {
				return files.stream()
					.filter(StringUtil::isNotEmpty)
					.map(p -> {
						String pathP = p;
						if(pathP.charAt(0) != '/') {
							pathP = '/' + pathP;
						}
						return pathP;
					})
					.filter(p -> {
						try {
							ServletMatch match = this.getServlet(p);
							if(match != null) {
								return true;
							}
							URL res = this.getResource(p);
							if(res != null) {
								return true;
							}
							
							return false;
						} catch(javax.servlet.ServletException | IOException e) {
							throw new RuntimeException(e);
						}
					})
					.findFirst()
					.orElse(null);
			}
		} else {
			return null;
		}
	}

	@Override
	protected void doDestroyModule() {
		this.initialized = false;

		try(var withCl = new WithClassLoader()) {
			ServletUtil.contextDestroyed(servletContext);
		} catch(Exception e) {
			if(log.isLoggable(Level.WARNING)) {
				log.log(Level.WARNING, MessageFormat.format("Encountered exception destroying ServletContext for {0}", this), e);
			}
		}
		
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
		
		try {
			if(!this.initialized) {
				throw new IllegalStateException(MessageFormat.format("Module {0} was not properly initialized", this));
			}
			
			super.doService(contextPath, pathInfo, httpSessionAdapter, servletRequest, servletResponse);
		} catch(PageNotFoundException e) {
			if(pathInfo.isEmpty() || "/".equals(pathInfo)) { //$NON-NLS-1$
				// Check for a welcome page and re-run the request with that
				String welcomePage = this.getWelcomePage();
				if(StringUtil.isNotEmpty(welcomePage)) {
					HttpServletRequestAdapter welcomeReq = new WelcomePageRequestAdapter(servletRequest, welcomePage);
					super.doService(contextPath, welcomePage, httpSessionAdapter, welcomeReq, servletResponse);
					return;
				}
			}
			throw e;
		}
	}

	@Override
	public boolean exists() {
		return true;
	}

	@Override
	public NSFJakartaModuleClassLoader getModuleClassLoader() {
		return this.moduleClassLoader;
	}
	
	public NSFJakartaFileSystem getRuntimeFileSystem() {
		return fileSystem;
	}

	@Override
	public URL getResource(String res) throws MalformedURLException {
		return this.fileSystem.getUrl(ModuleUtil.trimResourcePath(res))
			.orElseGet(() -> {
				// Check for META-INF/resources in embedded JARs
				// TODO skip check if the incoming path has META-INF or WEB-INF in it already
				// moduleClassLoader may be null when it itself is being initialized and the JVM calls getResources
				if(this.moduleClassLoader != null) {
					String metaResPath = PathUtil.concat("META-INF/resources", res, '/'); //$NON-NLS-1$
					return this.moduleClassLoader.getJarResource(metaResPath);
				} else {
					return null;
				}
			});
	}

	@Override
	public InputStream getResourceAsStream(String res) {
		return this.fileSystem.openStream(ModuleUtil.trimResourcePath(res))
			.orElseGet(() -> {
				String metaResPath = PathUtil.concat("META-INF/resources", res, '/'); //$NON-NLS-1$
				return this.moduleClassLoader.getJarResourceAsStream(metaResPath);
			});
	}

	@Override
	public Set<String> getResourcePaths(String res) {
		// This is looking for all resources strictly within the folder path,
		//   with a trailing "/" if it's a subfolder of it
		// TODO look for subfolders?
		Stream<String> matches = this.getRuntimeFileSystem().listFiles(res);
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
		Map<String, LCDAdapterHttpSession> sessions = this.getSessions();
		synchronized(sessions) {
            for(LCDAdapterHttpSession var4 : sessions.values()) {
               this.notifySessionRemoved(var4);
            }

            sessions.clear();
         }
		
		doDestroyModule();
		doInitModule();
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
	
	// These are called by AdapterInvoker
	
	@Override
	public ServletMatch getServlet(String path) throws javax.servlet.ServletException {
		// TODO init Servlets at startup, at least those marked with an interface
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

		ServletUtil.getListeners(this.servletContext, ServletRequestListener.class)
			.forEach(l -> l.requestInitialized(new ServletRequestEvent(this.servletContext, request)));
		try {
			super.invokeServlet(servlet, req, resp);
		} catch(Throwable t) {
			if(log.isLoggable(Level.WARNING)) {
				log.log(Level.WARNING, MessageFormat.format("Encountered exception invoking Servlet {0} in {1}", servlet, this), t);
			}
			throw t;
		} finally {
			ServletUtil.getListeners(this.servletContext, ServletRequestListener.class)
				.forEach(l -> l.requestDestroyed(new ServletRequestEvent(this.servletContext, request)));
			HttpContextBean.setThreadResponse(null);
		}
	}
	
	@Override
	public void notifySessionAdded(LCDAdapterHttpSession session) {
		super.notifySessionAdded(session);
		
		// TODO Init CDI session

		ServletUtil.getListeners(this.servletContext, HttpSessionListener.class)
			.forEach(l -> l.sessionDestroyed(new HttpSessionEvent(ServletUtil.oldToNew(session))));
	}
	
	@Override
	public void notifySessionRemoved(LCDAdapterHttpSession session) {
		// TODO Term CDI session
		
		ServletUtil.getListeners(this.servletContext, HttpSessionListener.class)
			.forEach(l -> l.sessionDestroyed(new HttpSessionEvent(ServletUtil.oldToNew(session))));
		
		super.notifySessionRemoved(session);
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
		// TODO look for xsp.expires app property?
		// TODO check for image types like NSFComponentModule?
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
				BackendBridge.setNoRecycle(session, session, true);
				Database database = NSFModuleUtil.openDatabase(session, this.mapping.nsfPath());
				BackendBridge.setNoRecycle(session, database, true);
				XSPNative.setContextDatabase(session, XSPNative.getDBHandle(database));
				
				// Use the signer of xsp.properties - failing that, the server
				long hSigner = NotesUtil.createUserNameList(this.xspSigner);
				Session sessionAsSigner = XSPNative.createXPageSessionExt(this.xspSigner, hSigner, false, nativeCtx.getPreviewServer(), false);
				BackendBridge.setNoRecycle(sessionAsSigner, sessionAsSigner, true);
				Session sessAsSignerFullAccess = XSPNative.createXPageSessionExt(this.xspSigner, hSigner, false, nativeCtx.getPreviewServer(), false);
				BackendBridge.setNoRecycle(sessAsSignerFullAccess, sessAsSignerFullAccess, true);
				
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
	
	public LSXBEHolder withSession(String name) {
		try {
			Session session = XSPNative.createXPageSession(name, 0, false, true);
			BackendBridge.setNoRecycle(session, session, true);
			Database database = NSFModuleUtil.openDatabase(session, this.mapping.nsfPath());
			BackendBridge.setNoRecycle(session, database, true);
			XSPNative.setContextDatabase(session, XSPNative.getDBHandle(database));
			
			// Use the signer of xsp.properties - failing that, the server
			long hSigner = NotesUtil.createUserNameList(this.xspSigner);
			Session sessionAsSigner = XSPNative.createXPageSessionExt(this.xspSigner, hSigner, false, false, false);
			BackendBridge.setNoRecycle(sessionAsSigner, sessionAsSigner, true);
			Session sessAsSignerFullAccess = XSPNative.createXPageSessionExt(this.xspSigner, hSigner, false, false, false);
			BackendBridge.setNoRecycle(sessAsSignerFullAccess, sessAsSignerFullAccess, true);
			
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
	}
	
	private class WithClassLoader implements AutoCloseable {
		private final ClassLoader tccl;
		
		public WithClassLoader() {
			this.tccl = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(getModuleClassLoader());
		}
		
		@Override
		public void close() {
			Thread.currentThread().setContextClassLoader(tccl);
		}
	}
}
