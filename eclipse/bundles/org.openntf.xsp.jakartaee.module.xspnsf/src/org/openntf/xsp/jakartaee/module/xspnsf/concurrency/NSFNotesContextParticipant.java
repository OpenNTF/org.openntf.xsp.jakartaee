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
package org.openntf.xsp.jakartaee.module.xspnsf.concurrency;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.domino.xsp.module.nsf.NSFComponentModule;
import com.ibm.domino.xsp.module.nsf.NotesContext;

import org.glassfish.concurro.spi.ContextHandle;
import org.openntf.xsp.jakarta.concurrency.AttributedContextHandle;
import org.openntf.xsp.jakarta.concurrency.ContextSetupParticipant;

import jakarta.annotation.Priority;

/**
 * This {@link ContextSetupParticipant} will initialize and terminate an NSF-specific
 * {@link NotesContext} for the running thread.
 *
 * @author Jesse Gallagher
 * @since 2.7.0
 */
@Priority(100)
public class NSFNotesContextParticipant implements ContextSetupParticipant {
	public static final String ATTR_MODULE = NSFNotesContextParticipant.class.getName() + "_module"; //$NON-NLS-1$
	public static final String ATTR_CLASSLOADER = NSFNotesContextParticipant.class.getName() + "_classLoader"; //$NON-NLS-1$
	public static final String ATTR_REQUEST = NSFNotesContextParticipant.class.getName() + "_request"; //$NON-NLS-1$

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
	public void saveContext(final ContextHandle contextHandle) {
		if(contextHandle instanceof AttributedContextHandle ach) {
			NotesContext ctx = NotesContext.getCurrentUnchecked();
			if(ctx != null) {
				ach.setAttribute(ATTR_MODULE, ctx.getModule());
				getHttpServletRequest(ctx).ifPresent(request -> {
					ach.setAttribute(ATTR_REQUEST, request);
				});
			}
		}
	}

	@Override
	public void saveContext(final ContextHandle contextHandle, final Map<String, String> contextObjectProperties) {
		saveContext(contextHandle);
	}

	@Override
	public void setup(final ContextHandle contextHandle) throws IllegalStateException {
		if(!shouldSetup()) {
			return;
		}

		if(contextHandle instanceof AttributedContextHandle ach) {
			ComponentModule mod = ach.getAttribute(ATTR_MODULE);
			if(mod instanceof NSFComponentModule nsfMod) {
				mod.updateLastModuleAccess();

				NotesContext notesContext = new NotesContext(nsfMod);

				HttpServletRequest request = ach.getAttribute(ATTR_REQUEST);
				if(request != null) {
					try {
						notesContext.initRequest(request);
					} catch (ServletException e) {
						e.printStackTrace();
					}
				}

				NotesContext.initThread(notesContext);

				ClassLoader cl = AccessController.doPrivileged((PrivilegedAction<ClassLoader>)() -> {
					ClassLoader tccc = Thread.currentThread().getContextClassLoader();
					Thread.currentThread().setContextClassLoader(mod.getModuleClassLoader());
					return tccc;
				});
				ach.setAttribute(ATTR_CLASSLOADER, cl);
			}
		}
	}

	@Override
	public void reset(final ContextHandle contextHandle) {
		if(contextHandle instanceof AttributedContextHandle ach) {
			ComponentModule mod = ach.getAttribute(ATTR_MODULE);
			if(mod instanceof NSFComponentModule) {
				ClassLoader tccc = ach.getAttribute(ATTR_CLASSLOADER);
				if(tccc != null) {
					AccessController.doPrivileged((PrivilegedAction<Void>)() -> {
						Thread.currentThread().setContextClassLoader(tccc);
						return null;
					});
				}

				if(NotesContext.getCurrentUnchecked() != null) {
					NotesContext.termThread();
				}
			}
		}
	}

	private boolean shouldSetup() {
		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
		return Arrays.stream(stack)
			.anyMatch(el -> ThreadPoolExecutor.class.getName().equals(el.getClassName()));
	}

	protected Optional<HttpServletRequest> getHttpServletRequest(final NotesContext context) {
		return AccessController.doPrivileged((PrivilegedAction<Optional<HttpServletRequest>>)() -> {
			try {
				return Optional.ofNullable((HttpServletRequest)notesContextRequestField.get(context));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		});
	}
}
