/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakarta.cdi.context;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

import javax.faces.context.FacesContext;

import org.openntf.xsp.jakarta.cdi.context.BasicScopeContextHolder.BasicScopeInstance;
import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.2.0
 */
@SuppressWarnings("serial")
public abstract class AbstractProxyingContext implements Context, Serializable {
	
	private static final ThreadLocal<HttpServletRequest> THREAD_REQUESTS = new ThreadLocal<>();
	
	
	public static void setThreadContextRequest(HttpServletRequest request) {
		THREAD_REQUESTS.set(request);
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
	
	protected Optional<HttpServletRequest> getHttpServletRequest() {
		if(THREAD_REQUESTS.get() != null) {
			return Optional.of(THREAD_REQUESTS.get());
		}
		
		// Check the active session
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if(facesContext != null) {
			javax.servlet.ServletContext context = (javax.servlet.ServletContext)facesContext.getExternalContext().getContext();
			javax.servlet.http.HttpServletRequest request = (javax.servlet.http.HttpServletRequest)facesContext.getExternalContext().getRequest();
			return Optional.ofNullable(ServletUtil.oldToNew(context, request));
		}
		
		return LibraryUtil.findExtensionsSorted(ComponentModuleLocator.class, false)
			.stream()
			.map(ComponentModuleLocator::getServletRequest)
			.filter(Optional::isPresent)
			.findFirst()
			.map(Optional::get);
	}
}
