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
import java.util.Collection;
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
import org.openntf.xsp.jakartaee.module.jakartansf.util.NSFModuleUtil;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.DominoQuery;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
import lotus.domino.Session;

/**
 * @since 3.4.0
 */
public class NSFJakartaModuleService extends HttpService {
	private static final Logger log = Logger.getLogger(NSFJakartaModuleService.class.getPackageName());
	
	private static final String ENV_NSF_PATH = "Jakarta_ConfigNSF"; //$NON-NLS-1$
	private static final String NSF_PATH_DEFAULT = "jakartaconfig.nsf"; //$NON-NLS-1$
	private static final String FORM_MODULE = "JakartaNSFModule"; //$NON-NLS-1$
	private static final String ITEM_WEBPATH = "WebPath"; //$NON-NLS-1$
	private static final String ITEM_NSFPATH = "NSFPath"; //$NON-NLS-1$
	private static final String ITEM_SERVERS = "Servers"; //$NON-NLS-1$
	
	private static final int MAX_REFRESH_ATTEMPTS = 10;
	
	private final Map<String, NSFJakartaModule> modules;
	private final ExecutorService exec;

	public NSFJakartaModuleService(LCDEnvironment env) {
		super(env);
		
		this.exec = Executors.newCachedThreadPool(NotesThread::new);
		
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
				Collection<String> namesList = getNamesList(serverName);
				
				String configPath = session.getEnvironmentString(ENV_NSF_PATH, true);
				if(StringUtil.isEmpty(configPath)) {
					configPath = NSF_PATH_DEFAULT;
				}
				
				Database configDb = NSFModuleUtil.openDatabase(session, configPath);
				if(configDb == null || !configDb.isOpen()) {
					// Exit early with a note
					if(log.isLoggable(Level.FINE)) {
						log.fine(MessageFormat.format("{0}: Unable to open Jakarta config NSF at path {1}; skipping module initialization", getClass().getSimpleName(), configPath));
					}
					
					return result;
				}
				
				DominoQuery query = configDb.createDominoQuery();
				query.setNamedVariable("form", FORM_MODULE); //$NON-NLS-1$
				DocumentCollection moduleDocs = query.execute("Form = ?form"); //$NON-NLS-1$
				
				Document moduleDoc = moduleDocs.getFirstDocument();
				while(moduleDoc != null) {
					List<?> servers = moduleDoc.getItemValue(ITEM_SERVERS);
					boolean isValid = servers.stream()
						.anyMatch(server -> namesList.contains(server));
					
					String webPath = moduleDoc.getItemValueString(ITEM_WEBPATH);
					if(isValid) {
						if(!webPath.isEmpty() && !"/".equals(webPath)) { //$NON-NLS-1$
							String barePath = webPath;
							if(barePath.indexOf('/') == 0) {
								barePath = barePath.substring(1);
							}
							
							String nsfPath = moduleDoc.getItemValueString(ITEM_NSFPATH);
							if(StringUtil.isNotEmpty(nsfPath)) {
								result.add(new ModuleMap(nsfPath, barePath));
							} else {
								if(log.isLoggable(Level.WARNING)) {
									log.warning(MessageFormat.format("{0}: Skipping invalid NSF path for module \"{1}\"", getClass().getSimpleName(), webPath));
								}
							}
							
						} else {
							if(log.isLoggable(Level.WARNING)) {
								log.warning(MessageFormat.format("{0}: Skipping invalid module path \"{1}\"", getClass().getSimpleName(), webPath));
							}
						}
					} else {
						if(log.isLoggable(Level.FINEST)) {
							log.finest(MessageFormat.format("{0}: Skipping module {1} for non-matching servers", getClass().getSimpleName(), webPath));
						}
					}
					
					Document tempDoc = moduleDoc;
					moduleDoc = moduleDocs.getNextDocument();
					tempDoc.recycle();
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
	
	@SuppressWarnings("unchecked")
	private Collection<String> getNamesList(String userName) {
		try {
			// DominoServer is blocked by our normal ClassLoader, so use the root one reflectively
			Object server = Class.forName("lotus.notes.addins.DominoServer", true, ClassLoader.getSystemClassLoader()).getConstructor().newInstance(); //$NON-NLS-1$
			return (Collection<String>)server.getClass().getMethod("getNamesList", String.class).invoke(server, userName); //$NON-NLS-1$
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

}
