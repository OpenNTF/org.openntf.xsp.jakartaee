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
package org.openntf.xsp.jakartaee.module.osgi;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.domino.osgi.core.context.ContextInfo;
import com.ibm.domino.xsp.adapter.osgi.AbstractOSGIModule;
import com.ibm.domino.xsp.adapter.osgi.NotesContext;

import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;

import jakarta.annotation.Priority;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import lotus.domino.Database;
import lotus.domino.Session;

/**
 * Locates an active {@link AbstractOSGIModule} when the current request
 * is in an OSGi Servlet or WebContainer context.
 *
 * @author Jesse Gallagher
 * @since 2.8.0
 */
@Priority(2)
public class OSGiComponentModuleLocator implements ComponentModuleLocator {
	private static final Field osgiNotesContextRequestField;
	private static final Field osgiNotesContextModuleField;
	static {
		Field[] request = new Field[1];
		Field[] module = new Field[1];
		AccessController.doPrivileged((PrivilegedAction<Void>)() -> {
			Class<?> osgiContextClass = null;
			try {
				osgiContextClass = Class.forName("com.ibm.domino.xsp.adapter.osgi.NotesContext"); //$NON-NLS-1$
			} catch (ClassNotFoundException e1) {
				// In Notes or other non-full environment
				return null;
			}
			try {
				Field field = osgiContextClass.getDeclaredField("request"); //$NON-NLS-1$
				field.setAccessible(true);
				request[0] = field;

				field = osgiContextClass.getDeclaredField("module"); //$NON-NLS-1$
				field.setAccessible(true);
				module[0] = field;
			} catch (NoSuchFieldException | SecurityException e) {
				throw new RuntimeException(e);
			}

			return null;
		});

		osgiNotesContextRequestField = request[0];
		osgiNotesContextModuleField = module[0];
	}
	private static final Logger log = Logger.getLogger(OSGiComponentModuleLocator.class.getPackage().getName());

	private boolean isAvailable() {
		return osgiNotesContextRequestField != null;
	}

	@Override
	public boolean isActive() {
		return isAvailable() && NotesContext.getCurrentUnchecked() != null;
	}

	@Override
	public AbstractOSGIModule getActiveModule() {
		if(!isAvailable()) {
			return null;
		}
		NotesContext osgiContext = NotesContext.getCurrentUnchecked();
		if(osgiContext != null) {
			try {
				return (AbstractOSGIModule)osgiNotesContextModuleField.get(osgiContext);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	@Override
	public Optional<ServletContext> getServletContext() {
		if(!isActive()) {
			return Optional.empty();
		}
		// TODO determine if this is useful when working in a WebContainer app
		AbstractOSGIModule module = getActiveModule();
		javax.servlet.ServletContext ctx = module.getServletContext();
		String contextPath = getServletRequest().get().getContextPath();
		return Optional.ofNullable(ServletUtil.oldToNew(contextPath, ctx));
	}

	@Override
	public Optional<HttpServletRequest> getServletRequest() {
		if(!isActive()) {
			return Optional.empty();
		}
		NotesContext osgiContext = NotesContext.getCurrentUnchecked();
		if(osgiContext != null) {
			try {
				javax.servlet.http.HttpServletRequest request = (javax.servlet.http.HttpServletRequest)osgiNotesContextRequestField.get(osgiContext);
				javax.servlet.ServletContext servletContext = getActiveModule().getServletContext();
				return Optional.of(ServletUtil.oldToNew(servletContext, request));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<String> getVersion() {
		return Optional.empty();
	}

	@Override
	public Optional<NotesDatabase> getNotesDatabase() {
		try {
			return Optional.ofNullable(ContextInfo.getServerDatabase());
		} catch (NotesAPIException e) {
			if(log.isLoggable(Level.SEVERE)) {
				log.log(Level.SEVERE, "Encountered exception trying to open the context database", e);
			}
			return Optional.empty();
		}
	}

	@Override
	public Optional<Database> getUserDatabase() {
		return Optional.ofNullable(ContextInfo.getUserDatabase());
	}

	@Override
	public Optional<Session> getUserSession() {
		return Optional.ofNullable(ContextInfo.getUserSession());
	}

	@Override
	public Optional<Session> getSessionAsSigner() {
		return Optional.empty();
	}

	@Override
	public Optional<Session> getSessionAsSignerWithFullAccess() {
		return Optional.empty();
	}

}
