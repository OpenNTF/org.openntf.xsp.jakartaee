/**
 * Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakartaee.module.jakartansf.util;

import java.io.IOException;
import java.io.StringReader;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.ibm.commons.util.StringUtil;

import org.openntf.xsp.jakartaee.module.jakartansf.NSFJakartaModule;
import org.openntf.xsp.jakartaee.module.jakartansf.NSFJakartaModuleService;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.DominoQuery;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.Session;

/**
 * @since 3.4.0
 */
public enum ModuleTracker {
	INSTANCE;
	
	private static final Logger log = System.getLogger(ModuleTracker.class.getPackageName());
	
	private static final String ENV_NSF_PATH = "Jakarta_ConfigNSF"; //$NON-NLS-1$
	private static final String NSF_PATH_DEFAULT = "jakartaconfig.nsf"; //$NON-NLS-1$
	private static final String FORM_MODULE = "JakartaNSFModule"; //$NON-NLS-1$
	private static final String ITEM_WEBPATH = "WebPath"; //$NON-NLS-1$
	private static final String ITEM_NSFPATH = "NSFPath"; //$NON-NLS-1$
	private static final String ITEM_SERVERS = "Servers"; //$NON-NLS-1$
	private static final String ITEM_MPCONFIG = "MPConfig"; //$NON-NLS-1$

	private final Map<String, NSFJakartaModule> modules = new ConcurrentHashMap<>();
	private final CountDownLatch initLatch = new CountDownLatch(1);
	
	public void populateModules(NSFJakartaModuleService service) {
		List<ModuleMap> matches = findMappings();
		matches.stream()
			.map(match -> new NSFJakartaModule(service.getEnvironment(), service, match))
			.filter(Objects::nonNull)
			.forEach(mod -> this.modules.put(mod.getMapping().path(), mod));
	}
	
	public void initializeModules() {
		NSFJakartaModuleService.exec.submit(() -> {
			try {
				this.modules.values().stream()
					.map(module -> (Runnable)() -> {
						log.log(Level.TRACE, () -> MessageFormat.format("Initializing module {0}", module));
						
						try(var ctx = ActiveRequest.with(new ActiveRequest(module, null, null))) {
							module.initModule();
						} catch(Exception e) {
							log.log(Level.ERROR, () -> MessageFormat.format("Encountered exception initializing module {0}", module), e);
						}
						log.log(Level.TRACE, () -> MessageFormat.format("Finished initializing module {0}", module));
					})
					.forEach(NSFJakartaModuleService.exec::submit);
				
				log.log(Level.TRACE, () -> MessageFormat.format("Initialized {0} with mappings {1}", getClass().getSimpleName(), modules));
			} catch(Throwable t) {
				log.log(Level.ERROR, "Encountered exception initializing NSFJakartaModuleService", t);
				throw t;
			} finally {
				initLatch.countDown();
			}
		});
	}
	
	public Map<String, NSFJakartaModule> getModules() {
		return modules;
	}
	
	public void awaitInit() {
		try {
			if(!this.initLatch.await(3, TimeUnit.MINUTES)) {
				throw new IllegalStateException("Timed out waiting for service initialization");
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	private List<ModuleMap> findMappings() {
		try {
			List<ModuleMap> result = new ArrayList<>();
			
			Session session = NotesFactory.createSession();
			try {
				String serverName = session.getServerName();
				Collection<String> namesList = NSFModuleUtil.getNamesList(serverName);
				
				String configPath = session.getEnvironmentString(ENV_NSF_PATH, true);
				if(StringUtil.isEmpty(configPath)) {
					configPath = NSF_PATH_DEFAULT;
				}
				
				Database configDb = NSFModuleUtil.openDatabase(session, configPath);
				if(configDb == null || !configDb.isOpen()) {
					// Exit early with a note
					String fConfigPath = configPath;
					log.log(Level.TRACE, () -> MessageFormat.format("{0}: Unable to open Jakarta config NSF at path {1}; skipping module initialization", getClass().getSimpleName(), fConfigPath));
					
					return result;
				}
				
				DominoQuery query = configDb.createDominoQuery();
				query.setNamedVariable("form", FORM_MODULE); //$NON-NLS-1$
				query.setNamedVariable("disabled", "N"); //$NON-NLS-1$ //$NON-NLS-2$
				DocumentCollection moduleDocs = query.execute("Form = ?form and not Enabled = ?disabled"); //$NON-NLS-1$
				
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
								// Read in any MP Config properties if present
								Properties props = new Properties();
								String mpConfigString = moduleDoc.getItemValueString(ITEM_MPCONFIG);
								if(StringUtil.isNotEmpty(mpConfigString)) {
									try {
										props.load(new StringReader(mpConfigString));
									} catch (IOException | IllegalArgumentException e) {
										log.log(Level.WARNING, () -> MessageFormat.format("{0}: Unable to read MP Config properties for module path {1}", getClass().getSimpleName(), webPath), e);
									}
								}
								
								result.add(new ModuleMap(nsfPath, barePath, props));
							} else {
								log.log(Level.WARNING, () -> MessageFormat.format("{0}: Skipping invalid NSF path for module \"{1}\"", getClass().getSimpleName(), webPath));
							}
							
						} else {
							log.log(Level.WARNING, () -> MessageFormat.format("{0}: Skipping invalid module path \"{1}\"", getClass().getSimpleName(), webPath));
						}
					} else {
						log.log(Level.DEBUG, () -> MessageFormat.format("{0}: Skipping module {1} for non-matching servers", getClass().getSimpleName(), webPath));
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
			log.log(Level.ERROR, "Encountered exception finding NSFJakartaModules", e);
			return Collections.emptyList();
		}
	}
}
