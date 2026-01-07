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
package org.openntf.xsp.jakartaee.module.jakartansf;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.ComponentModule.RestartModuleSignal;
import com.ibm.designer.runtime.domino.adapter.util.XSPErrorPage;
import com.ibm.designer.runtime.domino.adapter.HttpService;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletRequestAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletResponseAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpSessionAdapter;
import org.openntf.xsp.jakartaee.module.jakartansf.util.ActiveRequest;
import org.openntf.xsp.jakartaee.module.jakartansf.util.ModuleTracker;

import jakarta.servlet.http.HttpServletResponse;
import lotus.domino.NotesThread;

/**
 * @since 3.4.0
 */
public class NSFJakartaModuleService extends HttpService {
	private static final Logger log = Logger.getLogger(NSFJakartaModuleService.class.getPackageName());
	
	private static final int MAX_REFRESH_ATTEMPTS = 10;

	public static final ExecutorService exec = Executors.newCachedThreadPool(NotesThread::new);
	private static NSFJakartaModuleService instance;
	
	public synchronized static NSFJakartaModuleService getInstance(LCDEnvironment env) {
		if(instance == null) {
			LCDEnvironment realEnv = env == null ? LCDEnvironment.getInstance() : env;
			instance = new NSFJakartaModuleService(realEnv);
		}
		return instance;
	}

	public NSFJakartaModuleService(LCDEnvironment env) {
		super(env);
		
		instance = this;
		
		ModuleTracker.INSTANCE.populateModules(this);
	}

	@Override
	public void getModules(List<ComponentModule> env) {
		ModuleTracker.INSTANCE.awaitInit();
		env.addAll(ModuleTracker.INSTANCE.getModules().values());
	}
	
	public Optional<NSFJakartaModule> getModule(String mappingPath) {
		ModuleTracker.INSTANCE.awaitInit();
		
		NSFJakartaModule module = ModuleTracker.INSTANCE.getModules().get(mappingPath);
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
		ModuleTracker.INSTANCE.awaitInit();
		String pathInfo = getChompedPathInfo(fullPath);
		boolean match = ModuleTracker.INSTANCE.getModules().keySet().stream()
			.anyMatch(contextRoot -> pathInfo.equals(contextRoot) || pathInfo.startsWith(contextRoot + '/'));
		return match;
	}

	@Override
	public boolean doService(String lcdContextPath, String pathInfo, HttpSessionAdapter httpSessionAdapter,
			HttpServletRequestAdapter servletRequest, HttpServletResponseAdapter servletResponse) throws ServletException, IOException {
		if (StringUtil.isEmpty(pathInfo)) {
			return false;
		}
		
		ModuleTracker.INSTANCE.awaitInit();
		
		String chompedPath = getChompedPathInfo(pathInfo);
		Optional<NSFJakartaModule> target = ModuleTracker.INSTANCE.getModules().entrySet()
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
				
				if(module.shouldRefresh()) {
					module.refresh();
				}
				try(
					var lsxbe = module.withSessions(servletRequest);
					var xtx = ActiveRequest.with(new ActiveRequest(module, lsxbe, null));
				) {
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
			} catch(Exception e) {
				// XspCmdManager performs an equivalent test to show "Item not found exception" 404 pages
				if(e.getClass().getName().contains("PageNotFoundException")) { //$NON-NLS-1$
					throw e;
				}
				
				servletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				servletResponse.setContentType("text/html"); //$NON-NLS-1$
				XSPErrorPage.handleException(servletResponse.getWriter(), e, "", false); //$NON-NLS-1$
				return true;
			} finally {
				NotesThread.stermThread();
				
				// Sanity check
				if(ActiveRequest.get().isPresent()) {
					throw new IllegalStateException("Finished doService with an ActiveRequest still on the stack");
				}
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
}
