/**
 * Copyright (c) 2018-2023 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakartaee.module;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Optional;

import org.openntf.xsp.jakartaee.servlet.ServletUtil;

import com.ibm.domino.xsp.module.nsf.NSFComponentModule;
import com.ibm.domino.xsp.module.nsf.NotesContext;

import jakarta.annotation.Priority;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Locates an active {@link NSFComponentModule} when the current request
 * is in an NSF context.
 * 
 * @author Jesse Gallagher
 * @since 2.8.0
 */
@Priority(1)
public class NSFComponentModuleLocator implements ComponentModuleLocator {
	private static final Field notesContextRequestField;
	static {
		notesContextRequestField = AccessController.doPrivileged((PrivilegedAction<Field>)() -> {
			try {
				Field field = NotesContext.class.getDeclaredField("httpRequest"); //$NON-NLS-1$
				field.setAccessible(true);
				return field;
			} catch (NoSuchFieldException | SecurityException e) {
				throw new RuntimeException(e);
			}
		});
	}
	
	@Override
	public boolean isActive() {
		return NotesContext.getCurrentUnchecked() != null;
	}

	@Override
	public NSFComponentModule getActiveModule() {
		NotesContext nsfContext = NotesContext.getCurrentUnchecked();
		if(nsfContext != null) {
			return nsfContext.getModule();
		}
		return null;
	}

	@Override
	public Optional<ServletContext> getServletContext() {
		NotesContext nsfContext = NotesContext.getCurrentUnchecked();
		if(nsfContext != null) {
			NSFComponentModule module = nsfContext.getModule();
			String path = module.getDatabasePath().replace('\\', '/');
			javax.servlet.ServletContext servletContext = module.getServletContext();
			return Optional.of(ServletUtil.oldToNew(path, servletContext));
		}
		return Optional.empty();
	}

	@Override
	public Optional<HttpServletRequest> getServletRequest() {
		return getServletContext()
			.flatMap(servletContext -> {
				NotesContext nsfContext = NotesContext.getCurrentUnchecked();
				if(nsfContext != null) {
					try {
						javax.servlet.http.HttpServletRequest request = (javax.servlet.http.HttpServletRequest)notesContextRequestField.get(nsfContext);
						return Optional.of(ServletUtil.oldToNew(ServletUtil.newToOld(servletContext), request));
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}
				return null;
			});
	}
	
	

}
