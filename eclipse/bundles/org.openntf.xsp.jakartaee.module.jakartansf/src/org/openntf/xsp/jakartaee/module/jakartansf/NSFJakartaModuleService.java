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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.ServletException;

import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.ComponentModule.RestartModuleSignal;
import com.ibm.designer.runtime.domino.adapter.HttpService;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletRequestAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletResponseAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpSessionAdapter;

import org.openntf.xsp.jakartaee.module.jakartansf.util.ActiveRequest;
import org.openntf.xsp.jakartaee.module.jakartansf.util.ModuleMap;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
import lotus.domino.Session;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewNavigator;

/**
 * @since 3.4.0
 */
public class NSFJakartaModuleService extends HttpService {
	private static final Logger log = Logger.getLogger(NSFJakartaModuleService.class.getPackageName());
	
	private static final String PREFIX_WEBPATH = "webpath="; //$NON-NLS-1$
	private static final int MAX_REFRESH_ATTEMPTS = 10;
	
	private final String catalogNsfPath;
	private final Map<String, NSFJakartaModule> modules;
	private final ExecutorService exec;

	public NSFJakartaModuleService(LCDEnvironment env) {
		super(env);
		
		this.exec = Executors.newCachedThreadPool(NotesThread::new);
		this.catalogNsfPath = findCatalogNsfPath();
		
		try {
			List<ModuleMap> matches = findMappings();
			this.modules = matches.stream()
				.map(match -> new NSFJakartaModule(env, this, match))
				.filter(Objects::nonNull)
				.collect(Collectors.toMap(mod -> mod.getMapping().path(), Function.identity()));
			
			this.modules.values().stream()
				.map(module -> (Runnable)() -> {
					log.fine(() -> MessageFormat.format("Initializing module {0}", module));
					ActiveRequest.set(new ActiveRequest(module, null, null));
					try {
						module.initModule();
					} catch(Exception e) {
						if(log.isLoggable(Level.WARNING)) {
							log.log(Level.WARNING, MessageFormat.format("Encountered exception initializing module {0}", module), e);
						}
					} finally {
						ActiveRequest.set(null);
					}
					log.fine(() -> MessageFormat.format("Finished initializing module {0}", module));
				})
				.forEach(exec::submit);
			
			log.info(() -> MessageFormat.format("Initialized {0} with mappings {1}", getClass().getSimpleName(), matches));
		} catch(Throwable t) {
			if(log.isLoggable(Level.SEVERE)) {
				log.log(Level.SEVERE, "Encountered exception initializing NSFJakartaModuleService", t);
			}
			throw t;
		}
	}
	

	@Override
	public void getModules(List<ComponentModule> env) {
		env.addAll(this.modules.values());
	}
	
	public Optional<NSFJakartaModule> getModule(String mappingPath) {
		NSFJakartaModule module = this.modules.get(mappingPath);
		return Optional.ofNullable(module);
	}
	
	@Override
	public void destroyService() {
		exec.shutdown();
		try {
			if(!exec.awaitTermination(3, TimeUnit.MINUTES)) {
				if(log.isLoggable(Level.WARNING)) {
					log.warning(MessageFormat.format("{0} executor did not terminate in a reasonable amount of time", getClass().getSimpleName()));
				}
			}
		} catch (InterruptedException e) {
			// Ignore
		}
	}
	
	@Override
	public boolean isXspUrl(String fullPath, boolean arg1) {
		String pathInfo = getChompedPathInfo(fullPath);
		boolean match = this.modules.keySet().stream()
			.anyMatch(contextRoot -> pathInfo.equals(contextRoot) || pathInfo.startsWith(contextRoot + '/'));
		return match;
	}

	@Override
	public boolean doService(String lcdContextPath, String pathInfo, HttpSessionAdapter httpSessionAdapter,
			HttpServletRequestAdapter servletRequest, HttpServletResponseAdapter servletResponse) throws ServletException, IOException {
		if (StringUtil.isEmpty(pathInfo)) {
			return false;
		}
		
		String chompedPath = getChompedPathInfo(pathInfo);
		Optional<NSFJakartaModule> target = this.modules.entrySet()
			.stream()
			.filter(entry -> chompedPath.equals(entry.getKey()) || chompedPath.startsWith(entry.getKey() + '/'))
			.map(Map.Entry::getValue)
			.findFirst();
		if (target.isPresent()) {
			// TODO consider running requests in the ExecutorService to allow for forced shutdown
			
			NotesThread.sinitThread();
			try {
				NSFJakartaModule module = target.get();
				
				String contextPath = PathUtil.concat(lcdContextPath, '/' + module.getMapping().path(), '/');
				String internalPathInfo = pathInfo.substring(contextPath.length());
				int i = 0;
				
				try(var lsxbe = module.withSessions(servletRequest)) {
					ActiveRequest.set(new ActiveRequest(module, lsxbe, null));
					
					while(i++ < MAX_REFRESH_ATTEMPTS) {
						try {
							module.doService(contextPath, internalPathInfo, httpSessionAdapter, servletRequest, servletResponse);
							return true;
						} catch(RestartModuleSignal s) {
							module.refresh();
						}
					}
				}
				throw new IllegalStateException(MessageFormat.format("Module didn't refresh after {0} attempts", MAX_REFRESH_ATTEMPTS));
			} finally {
				ActiveRequest.set(null);
				NotesThread.stermThread();
			}
		}
		return false;
	}
	
	// *******************************************************************************
	// * Internal implementation methods
	// *******************************************************************************
	
	private String getChompedPathInfo(String fullPath) {
		if (StringUtil.isEmpty(fullPath)) {
			return StringUtil.EMPTY_STRING;
		} else {
			int qIndex = fullPath.indexOf('?');
			if (qIndex >= 0) {
				return fullPath.substring(1, qIndex);
			} else {
				return fullPath.substring(1);
			}
		}
	}
	
	private List<ModuleMap> findMappings() {
		try {
			List<ModuleMap> result = new ArrayList<>();
			
			Session session = NotesFactory.createSession();
			try {
				String serverName = session.getServerName();
				
				Database catalog = session.getDatabase("", this.catalogNsfPath); //$NON-NLS-1$
				View byCategory = catalog.getView("ByCategory"); //$NON-NLS-1$
				byCategory.setAutoUpdate(false);
				
				ViewNavigator nav = byCategory.createViewNav();
				ViewEntry categoryEntry = nav.getFirst();
				while(categoryEntry != null) {
					if(categoryEntry.isCategory()) {
						List<?> columnValues = categoryEntry.getColumnValues();
						String catName = (String)columnValues.get(0);
						if(catName.toLowerCase().startsWith(PREFIX_WEBPATH)) {
							// Then look through its children to find the first that applies to this server
							ViewEntry childEntry = nav.getChild(categoryEntry);
							while(childEntry != null) {
							
								// We have to open the document because the category is proper-cased
								//   and the server name is CN only
								Document doc = childEntry.getDocument();
								if(serverName.equals(doc.getItemValueString("Server"))) { //$NON-NLS-1$
									@SuppressWarnings("unchecked")
									List<String> categories = doc.getItemValue("Categories"); //$NON-NLS-1$
									categories.stream()
										.filter(cat -> cat.startsWith(PREFIX_WEBPATH))
										.map(cat -> cat.substring(PREFIX_WEBPATH.length()))
										.filter(cat -> !cat.isEmpty() && !"/".equals(cat)) //$NON-NLS-1$
										.forEach(path -> {
											String barePath = path;
											if(barePath.indexOf('/') == 0) {
												barePath = barePath.substring(1);
											}
											
											String nsfPath;
											try {
												nsfPath = doc.getItemValueString("Pathname"); //$NON-NLS-1$
											} catch (NotesException e) {
												throw new RuntimeException(e);
											}
											
											result.add(new ModuleMap(nsfPath, barePath));
										});
								}
								doc.recycle();
							
								ViewEntry tempChild = childEntry;
								childEntry = nav.getNextSibling(childEntry);
								tempChild.recycle();
							}
						}
					}
					
					ViewEntry tempEntry = categoryEntry;
					categoryEntry = nav.getNextSibling(categoryEntry);
					tempEntry.recycle();
				}
			} finally {
				session.recycle();
			}
			
			return result;
		} catch(NotesException e) {
			e.printStackTrace();
			if(log.isLoggable(Level.SEVERE)) {
				log.log(Level.SEVERE, "Encountered exception finding NSFJakartaModules", e);
			}
			return Collections.emptyList();
		}
	}
	
	private String findCatalogNsfPath() {
		try {
			// DominoServer is blocked by our normal ClassLoader, so use the root one reflectively
			Object server = Class.forName("lotus.notes.addins.DominoServer", true, ClassLoader.getSystemClassLoader()).getConstructor().newInstance(); //$NON-NLS-1$
			Class<?> serverInfo = Class.forName("lotus.notes.addins.ServerInfo", true, ClassLoader.getSystemClassLoader()); //$NON-NLS-1$
			Object catalogInfo = serverInfo.getField("CATALOG").get(null); //$NON-NLS-1$
			List<?> info = (List<?>)server.getClass().getMethod("getInfo", serverInfo).invoke(server, catalogInfo); //$NON-NLS-1$
			return (String)info.get(0);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

}
