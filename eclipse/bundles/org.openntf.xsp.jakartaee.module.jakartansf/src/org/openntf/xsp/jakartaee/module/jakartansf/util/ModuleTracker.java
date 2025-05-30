package org.openntf.xsp.jakartaee.module.jakartansf.util;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

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
	
	private static final Logger log = Logger.getLogger(ModuleTracker.class.getPackageName());
	
	private static final String ENV_NSF_PATH = "Jakarta_ConfigNSF"; //$NON-NLS-1$
	private static final String NSF_PATH_DEFAULT = "jakartaconfig.nsf"; //$NON-NLS-1$
	private static final String FORM_MODULE = "JakartaNSFModule"; //$NON-NLS-1$
	private static final String ITEM_WEBPATH = "WebPath"; //$NON-NLS-1$
	private static final String ITEM_NSFPATH = "NSFPath"; //$NON-NLS-1$
	private static final String ITEM_SERVERS = "Servers"; //$NON-NLS-1$

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
						log.fine(() -> MessageFormat.format("Initializing module {0}", module));
						
						ActiveRequest.set(new ActiveRequest(module, null, null));
						try {
							module.initModule();
						} catch(Exception e) {
							if(log.isLoggable(Level.SEVERE)) {
								e.printStackTrace();
								log.log(Level.SEVERE, MessageFormat.format("Encountered exception initializing module {0}", module), e);
							}
						} finally {
							ActiveRequest.set(null);
						}
						log.fine(() -> MessageFormat.format("Finished initializing module {0}", module));
					})
					.forEach(NSFJakartaModuleService.exec::submit);
				
				if(log.isLoggable(Level.INFO)) {
					log.info(MessageFormat.format("Initialized {0} with mappings {1}", getClass().getSimpleName(), modules));
				}
			} catch(Throwable t) {
				if(log.isLoggable(Level.SEVERE)) {
					log.log(Level.SEVERE, "Encountered exception initializing NSFJakartaModuleService", t);
				}
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
}
