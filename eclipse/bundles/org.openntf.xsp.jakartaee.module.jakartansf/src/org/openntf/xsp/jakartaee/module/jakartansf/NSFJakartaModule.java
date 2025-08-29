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
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.commons.extension.ExtensionManager;
import com.ibm.commons.util.io.StreamUtil;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesConstants;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.designer.domino.napi.NotesNote;
import com.ibm.designer.domino.napi.NotesSession;
import com.ibm.designer.domino.napi.design.FileAccess;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;
import com.ibm.designer.runtime.domino.bootstrap.adapter.DominoHttpXspNativeContext;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletRequestAdapter;
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
import org.openntf.xsp.jakartaee.module.jakarta.AbstractJakartaModule;
import org.openntf.xsp.jakartaee.module.jakarta.DefaultModuleClassLoader;
import org.openntf.xsp.jakartaee.module.jakarta.ModuleFileSystem.FileEntry;
import org.openntf.xsp.jakartaee.module.jakarta.ModuleIcon;
import org.openntf.xsp.jakartaee.module.jakarta.ModuleIconSet;
import org.openntf.xsp.jakartaee.module.jakartansf.io.NSFJakartaFileSystem;
import org.openntf.xsp.jakartaee.module.jakartansf.io.NSFJakartaFileSystem.NSFMetadata;
import org.openntf.xsp.jakartaee.module.jakartansf.util.ActiveRequest;
import org.openntf.xsp.jakartaee.module.jakartansf.util.LSXBEHolder;
import org.openntf.xsp.jakartaee.module.jakartansf.util.ModuleMap;
import org.openntf.xsp.jakartaee.module.jakartansf.util.NSFModuleUtil;
import org.openntf.xsp.jakartaee.module.jakartansf.util.UncheckedNotesException;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.jakartaee.util.ModuleUtil;
import org.openntf.xsp.jakartaee.util.PriorityComparator;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HandlesTypes;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.HttpServletRequest;
import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
import lotus.domino.Session;

/**
 * @since 3.4.0
 */
public class NSFJakartaModule extends AbstractJakartaModule {
	private static final Logger log = Logger.getLogger(NSFJakartaModule.class.getPackageName());
	
	private final ModuleMap mapping;
	private NotesSession notesSession;
	private NotesDatabase notesDatabase;
	private NSFJakartaFileSystem fileSystem;
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
	public Optional<String> getModulePath() {
		return Optional.of(mapping.path());
	}
	
	@Override
	public ModuleIconSet getModuleIcons() {
		ModuleIconSet result = super.getModuleIcons();
		
		this.getRuntimeFileSystem().openStream("$DBIcon").ifPresent(is -> { //$NON-NLS-1$
			try {
				byte[] data;
				try {
					 data = is.readAllBytes();
				} finally {
					StreamUtil.close(is);
				}
				
				// Assume these are all 64x64, which was the case as of 12.0.0
				result.icons().add(new ModuleIcon("image/png", 64, 64, ByteBuffer.wrap(data))); //$NON-NLS-1$
			} catch(IOException e) {
				throw new UncheckedIOException(e);
			}
		});
		
		return result;
	}
	
	@Override
	public String getModuleTitle() {
		if(this.isInitialized()) {
			try(var lsxbe = withSession(xspSigner)) {
				return lsxbe.database().getTitle();
			} catch (NotesException e) {
				return this.getModuleName();
			}
		} else {
			return this.getModuleName();
		}
	}
	
	@Override
	public Optional<String> getMimeType(String filePath) {
		Optional<String> fs = getRuntimeFileSystem()
			.getEntry(filePath)
			.map(FileEntry::metadata)
			.map(NSFMetadata.class::cast)
			.map(NSFMetadata::mimeType);
		if(fs.isPresent()) {
			return fs;
		} else {
			return super.getMimeType(filePath);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doInitModule() {
		NotesThread.sinitThread();
		try {
			// Clear all attributes except temp dir
			Map<String, Object> attrs = this.getAttributes();
			Object tempDir = attrs.get("javax.servlet.context.tempdir"); //$NON-NLS-1$
			attrs.clear();
			attrs.put("javax.servlet.context.tempdir", tempDir); //$NON-NLS-1$
			attrs.put("jakarta.servlet.context.tempdir", tempDir); //$NON-NLS-1$
			
			ServletContext servletContext = ServletUtil.oldToNew('/' + this.mapping.path(), getServletContext());
			setJakartaServletContext(servletContext);
			
			try {
				clearModuleClassLoader();
				if(this.notesSession != null) {
					this.notesSession.recycle();
				}
				
				this.notesSession = new NotesSession();
				this.notesDatabase = notesSession.getDatabaseByPath(this.mapping.nsfPath());
				this.notesDatabase.open();
				if(!this.notesDatabase.isValidHandle()) {
					throw new RuntimeException(MessageFormat.format("Unable to open database {0}", this.mapping.nsfPath()));
				}
				
				// Try opening any view with LSXBE first to make sure the design collection is initialized
				Session session = NotesFactory.createSession();
				try {
					Database db = NSFModuleUtil.openDatabase(session, this.mapping.nsfPath());
					db.recycle(db.getViews());
				} finally {
					session.recycle();
				}
				
				this.fileSystem = new NSFJakartaFileSystem(this);
				
				if(!LibraryUtil.usesLibrary(LibraryUtil.LIBRARY_CORE, this)) {
					throw new IllegalStateException(MessageFormat.format("Module {0} is not configured for library {1}", this, LibraryUtil.LIBRARY_CORE));
				}
				
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

				setModuleClassLoader(new DefaultModuleClassLoader(this));
			} catch (NotesAPIException e) {
				e.printStackTrace();
				throw new RuntimeException(MessageFormat.format("Encountered exception 0x{0} initializing module {1}", Integer.toHexString(e.getNativeErrorCode()), this), e);
			} catch(NotesException e) {
				e.printStackTrace();
				throw new RuntimeException(MessageFormat.format("Encountered exception 0x{0} initializing module {1}", Integer.toHexString(e.id), this), e);
			} catch(Exception e) {
				throw new RuntimeException(MessageFormat.format("Encountered exception initializing module {0}", this), e);
			}

			// Register Servlet factories early, since initial
			Collection<JakartaIServletFactory> servletFactories = ExtensionManager.findApplicationServices(getModuleClassLoader(), "com.ibm.xsp.adapter.servletFactory").stream() //$NON-NLS-1$
				.filter(JakartaIServletFactory.class::isInstance)
				.map(JakartaIServletFactory.class::cast)
				.sorted(PriorityComparator.DESCENDING)
				.toList();
			setServletFactories(servletFactories);
			
			// Set lastRefresh, as used by ContainerUtil, before initializing CDI
			setLastRefresh(System.currentTimeMillis());

			// Find ServletContainerInitializers
			List<ServletContainerInitializer> initializers = new ArrayList<>(LibraryUtil.findExtensions(ServletContainerInitializer.class, this));
			LibraryUtil.findExtensions(ServletContainerInitializerProvider.class).forEach(provider -> {
				Collection<ServletContainerInitializer> inits = provider.provide(this);
				if(inits != null) {
					initializers.addAll(inits);
				}
				Collection<Class<? extends EventListener>> listeners = provider.provideListeners(this);
				if(listeners != null) {
					listeners.forEach(servletContext::addListener);
				}
			});
			
			// Find any declared or annotated listeners
			WebXml webXml = ServletUtil.getWebXml(this);
			for(String listenerClassName : webXml.getListeners()) {
				try {
					Class<? extends EventListener> c = (Class<? extends EventListener>) Class.forName(listenerClassName, true, getModuleClassLoader());
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
			
			ServletUtil.populateWebXmlParams(this, servletContext);
			
			try(
				var withCl = new WithClassLoader();
				var lsxbe = this.withSession(this.xspSigner);
				var ctx = ActiveRequest.with(new ActiveRequest(this, lsxbe, null))
			) {
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
				
				servletFactories.forEach(fac -> fac.init(this));
			}
			
			this.setInitialized(true);
		} finally {
			NotesThread.stermThread();
			closeInitLatch();
		}
	}
	
	public String getXspSigner() {
		return xspSigner;
	}

	@Override
	protected void doDestroyModule() {
		setInitialized(false);

		try(var withCl = new WithClassLoader()) {
			ServletUtil.contextDestroyed(getJakartaServletContext());
		} catch(Exception e) {
			if(log.isLoggable(Level.WARNING)) {
				log.log(Level.WARNING, MessageFormat.format("Encountered exception destroying ServletContext for {0}", this), e);
			}
		}
		
		// TODO move to a ServletContextListener in CDI
		if(ContainerUtil.getContainer(this) instanceof AutoCloseable cdi) {
			try {
				cdi.close();
			} catch(Exception e) {
				if(log.isLoggable(Level.WARNING)) {
					log.log(Level.WARNING, MessageFormat.format("Encountered exception closing CDI container for {0}", this), e);
				}
			}
		}
		
		clearModuleClassLoader();
		
		try {
			this.notesSession.recycle();
			this.notesSession = null;
			this.notesDatabase = null;
		} catch (NotesAPIException e) {
			// Ignore
		}
	}
	
	@Override
	public NSFJakartaFileSystem getRuntimeFileSystem() {
		return fileSystem;
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
	protected void invokeServlet(javax.servlet.Servlet servlet, javax.servlet.http.HttpServletRequest req,
			javax.servlet.http.HttpServletResponse resp) throws javax.servlet.ServletException, IOException {

		// TODO move to a ServletRequestListener in CDI
		HttpContextBean.setThreadResponse(ServletUtil.oldToNew(resp));
		
		// Update the active request with the "true" request object
		HttpServletRequest request = ServletUtil.oldToNew(getServletContext(), req);
		ActiveRequest.pushRequest(request);
		try {
			super.invokeServlet(servlet, req, resp);
		} finally {
			HttpContextBean.setThreadResponse(null);
		}
	}
	
	@Override
	public String toString() {
		return MessageFormat.format("{0}: {1}", getClass().getSimpleName(), mapping);
	}
	
	public LSXBEHolder withSessions(HttpServletRequestAdapter req) {
		awaitInit();
		
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
}
