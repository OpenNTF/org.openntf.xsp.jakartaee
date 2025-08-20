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
package org.openntf.xsp.jakartaee.module.jakarta;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;
import com.ibm.commons.util.io.StreamUtil;
import com.ibm.commons.xml.DOMUtil;
import com.ibm.commons.xml.XMLException;
import com.ibm.commons.xml.XResult;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.HttpService;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;
import com.ibm.designer.runtime.domino.adapter.ServletMatch;
import com.ibm.designer.runtime.domino.adapter.servlet.LCDAdapterHttpSession;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletRequestAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletResponseAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpSessionAdapter;
import com.ibm.xsp.page.PageNotFoundException;

import org.apache.tomcat.util.descriptor.web.WebXml;
import org.openntf.xsp.jakartaee.module.JakartaIServletFactory;
import org.openntf.xsp.jakartaee.module.jakarta.ModuleFileSystem.FileEntry;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.jakartaee.util.ModuleUtil;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

/**
 * Contains common implementation details for Jakarta Modules.
 * 
 * @since 3.5.0
 */
public abstract class AbstractJakartaModule extends ComponentModule {
	private static final Logger log = Logger.getLogger(AbstractJakartaModule.class.getPackageName());
	
	private DefaultModuleClassLoader moduleClassLoader;
	private ServletContext servletContext;
	private long lastRefresh;
	private Collection<JakartaIServletFactory> servletFactories;
	private CountDownLatch initLatch;
	private boolean initialized;
	
	public AbstractJakartaModule(LCDEnvironment env, HttpService service, String name, boolean persistentSessions) {
		super(env, service, name, persistentSessions);
		this.initLatch = new CountDownLatch(1);
	}
	
	protected void closeInitLatch() {
		this.initLatch.countDown();
	}
	
	public boolean isInitialized() {
		return initialized;
	}
	
	protected void setInitialized(boolean initialized) {
		this.initialized = initialized;
	}
	
	// App Information
	
	/**
	 * Retrieves an icon set for this module, if applicable.
	 * 
	 * @return a {@link ModuleIconSet} describing the module's icons,
	 *         which may be empty
	 */
	public ModuleIconSet getModuleIcons() {
		if(this.isInitialized()) {
			// Read web.xml directly since Tomcat's doesn't include icon info
			List<ModuleIcon> icons = new ArrayList<>();
			ModuleFileSystem fs = this.getRuntimeFileSystem();
			fs.openStream("WEB-INF/web.xml").ifPresent(is -> { //$NON-NLS-1$
				try {
					org.w3c.dom.Document webXml = DOMUtil.createDocument(is);
					XResult icon = DOMUtil.evaluateXPath(webXml, "/web-app/icon"); //$NON-NLS-1$
					if(!icon.isEmpty()) {
						if(icon.getSingleNode() instanceof Element iconNode) {
							NodeList largeIcons = iconNode.getElementsByTagName("large-icon"); //$NON-NLS-1$
							if(largeIcons.getLength() > 0) {
								String name = largeIcons.item(0).getTextContent();
								this.readIcon(name, 32, 32).ifPresent(icons::add);
							};
							NodeList smallIcons = iconNode.getElementsByTagName("small-icon"); //$NON-NLS-1$
							if(smallIcons.getLength() > 0) {
								String name = smallIcons.item(0).getTextContent();
								this.readIcon(name, 16, 16).ifPresent(icons::add);
							}
						}
					}
					
				} catch (XMLException e) {
					if(log.isLoggable(Level.WARNING)) {
						log.log(Level.WARNING, MessageFormat.format("Encountered exception reading web.xml in {0}", this), e);
					}
				} finally {
					StreamUtil.close(is);
				}
			});
			return new ModuleIconSet(icons);
		}
		return new ModuleIconSet(Collections.emptySet());
	}
	
	/**
	 * Retrieves the path that this module is mounted to on the server,
	 * if applicable.
	 * 
	 * @return an {@link Optional} describing the app mount path, or
	 *         an empty one if that is not applicable
	 */
	public abstract Optional<String> getModulePath();
	
	/**
	 * Retrieves a human-readable title for this module, which may be
	 * distinct from {@link #getModuleName()}.
	 * 
	 * @return a human-readable module title
	 */
	public String getModuleTitle() {
		if(this.isInitialized()) {
			WebXml webXml = ServletUtil.getWebXml(this);
			String displayName = webXml.getDisplayName();
			if(StringUtil.isNotEmpty(displayName)) {
				return displayName;
			}
		}
		return this.getModuleName();
	}

	public abstract ModuleFileSystem getRuntimeFileSystem();
	
	@Override
	public DefaultModuleClassLoader getModuleClassLoader() {
		return this.moduleClassLoader;
	}
	
	protected void setModuleClassLoader(DefaultModuleClassLoader moduleClassLoader) {
		this.moduleClassLoader = moduleClassLoader;
	}
	
	protected void setServletFactories(Collection<JakartaIServletFactory> servletFactories) {
		this.servletFactories = servletFactories;
	}
	
	// ComponentModule implementation
	
	// Called if getServlet returns null
	// We return false here because we don't want to interfere with
	//   existing third-party libraries that assume NSFComponentModule
	@Override
	public boolean hasServletFactories() {
		return false;
	}
	
	public Collection<JakartaIServletFactory> getServletFactories() {
		return servletFactories;
	}

	@Override
	public boolean exists() {
		return true;
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
	public void notifySessionAdded(LCDAdapterHttpSession session) {
		super.notifySessionAdded(session);
		
		// TODO Init CDI session

		ServletUtil.getListeners(getJakartaServletContext(), HttpSessionListener.class)
			.forEach(l -> l.sessionDestroyed(new HttpSessionEvent(ServletUtil.oldToNew(session))));
	}
	
	@Override
	public void notifySessionRemoved(LCDAdapterHttpSession session) {
		// TODO Term CDI session
		
		ServletUtil.getListeners(getJakartaServletContext(), HttpSessionListener.class)
			.forEach(l -> l.sessionDestroyed(new HttpSessionEvent(ServletUtil.oldToNew(session))));
		
		super.notifySessionRemoved(session);
	}
	
	@Override
	public void doService(String contextPath, String pathInfo, HttpSessionAdapter httpSessionAdapter, HttpServletRequestAdapter servletRequest,
			HttpServletResponseAdapter servletResponse) throws javax.servlet.ServletException, IOException {
		if(log.isLoggable(Level.FINER)) {
			log.finer(MessageFormat.format("{0}#doService with contextPath={1}, pathInfo={2}", getClass().getSimpleName(), contextPath, pathInfo));
		}
		
		awaitInit();
		
		try {
			super.doService(contextPath, pathInfo, httpSessionAdapter, servletRequest, servletResponse);
		} catch(PageNotFoundException | com.ibm.designer.runtime.domino.adapter.util.PageNotFoundException e) {
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

	// These are called by AdapterInvoker
	
	@Override
	public ServletMatch getServlet(String path) throws javax.servlet.ServletException {
		// TODO init Servlets at startup, at least those marked with an interface
		for(JakartaIServletFactory fac : getServletFactories()) {
			ServletMatch servletMatch = fac.getServletMatch('/' + getModulePath().get(), path);
			if(servletMatch != null) {
				return servletMatch;
			}
		}
		return null;
	}
	
	// Called by AdapterInvoker if getServlet or a ServletFactory returns a ServletMatch
	@Override
	protected void invokeServlet(javax.servlet.Servlet servlet, javax.servlet.http.HttpServletRequest req,
			javax.servlet.http.HttpServletResponse resp) throws javax.servlet.ServletException, IOException {
		if(log.isLoggable(Level.FINE)) {
			log.fine(MessageFormat.format("Invoking Servlet {0}", servlet));
		}

		ServletContext servletContext = getJakartaServletContext();
		HttpServletRequest request = ServletUtil.oldToNew(getServletContext(), req);
		ServletUtil.getListeners(servletContext, ServletRequestListener.class)
			.forEach(l -> l.requestInitialized(new ServletRequestEvent(servletContext, request)));
		try {
			super.invokeServlet(servlet, req, resp);
		} catch(Exception t) {
			if(log.isLoggable(Level.WARNING)) {
				log.log(Level.WARNING, MessageFormat.format("Encountered exception invoking Servlet {0} in {1}", servlet, this), t);
			}
			throw t;
		} finally {
			ServletUtil.getListeners(servletContext, ServletRequestListener.class)
				.forEach(l -> l.requestDestroyed(new ServletRequestEvent(servletContext, request)));
		}
	}

	@Override
	public URL getResource(String res) throws MalformedURLException {
		return getRuntimeFileSystem().getUrl(ModuleUtil.trimResourcePath(res))
			.orElse(null);
	}
	
	public URL getWebResource(String res) throws MalformedURLException {
		return getRuntimeFileSystem().getWebResourceUrl(ModuleUtil.trimResourcePath(res))
			.orElseGet(() -> {
				// Check for META-INF/resources in embedded JARs
				// TODO skip check if the incoming path has META-INF or WEB-INF in it already
				// moduleClassLoader may be null when it itself is being initialized and the JVM calls getResources
				DefaultModuleClassLoader moduleClassLoader = getModuleClassLoader();
				if(moduleClassLoader != null) {
					String metaResPath = PathUtil.concat("META-INF/resources", res, '/'); //$NON-NLS-1$
					return moduleClassLoader.getJarResource(metaResPath);
				} else {
					return null;
				}
			});
	}

	@Override
	public Set<String> getResourcePaths(String res) {
		// This is looking for all resources strictly within the folder path,
		//   with a trailing "/" if it's a subfolder of it
		// TODO look for subfolders?
		Stream<String> matches = this.getRuntimeFileSystem()
			.listFiles(res)
			.map(FileEntry::name);
		if(res.charAt(0) == '/') {
			matches = matches.map(p -> '/' + p);
		}
		return matches.collect(Collectors.toSet());
	}
	
	@Override
	protected void writeResource(ServletInvoker invoker, String res) throws IOException {
		// Do an early check here since otherwise the parent implementation will set a status of 200
		if(getWebResource(res) == null) {
			// TODO consider handling this differently, to avoid just "Item Not Found Exception" and a console log entry
			throw new PageNotFoundException(MessageFormat.format("No resource found at path {0}", res));
		} else {
			super.writeResource(invoker, res);
		}
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
	
	protected void setLastRefresh(long lastRefresh) {
		this.lastRefresh = lastRefresh;
	}

	@Override
	public InputStream getResourceAsStream(String res) {
		return getRuntimeFileSystem().openStream(ModuleUtil.trimResourcePath(res))
			.orElse(null);
	}
	
	// Called to serve resource - returns false if it doesn't exist
	@Override
	public boolean getResourceAsStream(OutputStream os, String res) {
		try(InputStream is = getRuntimeFileSystem().openStream(ModuleUtil.trimResourcePath(res))
		.orElseGet(() -> {
			String metaResPath = PathUtil.concat("META-INF/resources", res, '/'); //$NON-NLS-1$
			return getModuleClassLoader().getJarResourceAsStream(metaResPath);
		})) {
			if(is != null) {
				is.transferTo(os);
				return true;
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return false;
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
	
	protected void clearModuleClassLoader() {
		if(this.moduleClassLoader != null) {
			this.moduleClassLoader.close();
			this.moduleClassLoader = null;
		}
	}
	
	public ServletContext getJakartaServletContext() {
		return servletContext;
	}
	
	protected void setJakartaServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}
	
	private Optional<ModuleIcon> readIcon(String name, int width, int height) {
		if(StringUtil.isEmpty(name)) {
			return Optional.empty();
		}
		
		Optional<InputStream> iconIs = this.getRuntimeFileSystem().openStream(name);
		if(iconIs.isPresent()) {
			try(InputStream is = iconIs.get()) {
				byte[] iconData = is.readAllBytes();
				String mimeType = LibraryUtil.guessContentType(name);
				return Optional.of(new ModuleIcon(mimeType, width, height, ByteBuffer.wrap(iconData)));
			} catch (IOException e) {
				if(log.isLoggable(Level.WARNING)) {
					log.log(Level.WARNING, MessageFormat.format("Encountered exception reading icon {0} in {1}", name, this), e);
				}
			}
		}
		
		return Optional.empty();
	}
	
	protected void awaitInit() {
		try {
			if(!this.initLatch.await(3, TimeUnit.MINUTES)) {
				throw new IllegalStateException("Timed out waiting for module initialization");
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		if(!this.initialized) {
			throw new IllegalStateException(MessageFormat.format("Module {0} was not properly initialized", this));
		}
	}
	
	protected class WithClassLoader implements AutoCloseable {
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
