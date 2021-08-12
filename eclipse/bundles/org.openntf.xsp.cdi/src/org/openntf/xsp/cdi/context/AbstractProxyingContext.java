/**
 * Copyright © 2018-2021 Jesse Gallagher
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
package org.openntf.xsp.cdi.context;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.UUID;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.openntf.xsp.cdi.context.BasicScopeContextHolder.BasicScopeInstance;

import com.ibm.domino.xsp.adapter.osgi.NotesContext;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.2.0
 */
@SuppressWarnings("serial")
public abstract class AbstractProxyingContext implements Context, Serializable {
	
	private static final Field notesContextRequestField;
	static {
		notesContextRequestField = AccessController.doPrivileged((PrivilegedAction<Field>)() -> {
			try {
				Field field = NotesContext.class.getDeclaredField("request"); //$NON-NLS-1$
				field.setAccessible(true);
				return field;
			} catch (NoSuchFieldException | SecurityException e) {
				throw new RuntimeException(e);
			}
		});
	}
	
	private final String id = UUID.randomUUID().toString();

	protected abstract BasicScopeContextHolder getHolder();
	
	@SuppressWarnings("unchecked")
	@Override
	public synchronized <T> T get(final Contextual<T> contextual, final CreationalContext<T> creationalContext) {
		Bean<T> bean = (Bean<T>) contextual;
		BasicScopeContextHolder holder = getHolder();
		return (T) holder.getBeans().computeIfAbsent(bean.getBeanClass().getName(), className -> {
			BasicScopeInstance<T> instance = new BasicScopeInstance<>();
			instance.beanClass = className;
			instance.ctx = creationalContext;
			instance.instance = bean.create(creationalContext);
			return instance;
		}).instance;
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public synchronized <T> T get(final Contextual<T> contextual) {
		Bean<T> bean = (Bean<T>) contextual;
		BasicScopeContextHolder holder = getHolder();
		if(holder.getBeans().containsKey(bean.getBeanClass().getName())) {
			return (T)holder.getBean(bean.getBeanClass().getName()).instance;
		} else {
			return null;
		}
	}

	@Override
	public boolean isActive() {
		return true;
	}
	
	protected String generateKey() {
		return getClass().getName() + '-' + id;
	}
	
	protected HttpServletRequest getHttpServletRequest() {
		// Check the active session
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if(facesContext != null) {
			return (HttpServletRequest)facesContext.getExternalContext().getRequest();
		}
		
		// If we're not in a Faces context, check the OSGi servlet context
		NotesContext notesContext = NotesContext.getCurrentUnchecked();
		if(notesContext != null) {
			return getHttpServletRequest(notesContext);
		}
		
		return null;
	}
	
	protected HttpServletRequest getHttpServletRequest(NotesContext context) {
		return AccessController.doPrivileged((PrivilegedAction<HttpServletRequest>)() -> {
			try {
				return (HttpServletRequest)notesContextRequestField.get(context);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		});
	}
}
