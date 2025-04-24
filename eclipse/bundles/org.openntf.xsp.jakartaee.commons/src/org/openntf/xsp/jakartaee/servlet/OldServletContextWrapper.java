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
package org.openntf.xsp.jakartaee.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.ibm.designer.runtime.domino.adapter.IServletFactory;
import com.ibm.designer.runtime.domino.adapter.ServletMatch;

import org.openntf.xsp.jakartaee.MappingBasedServletFactory;
import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.FilterRegistration.Dynamic;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextAttributeEvent;
import jakarta.servlet.ServletContextAttributeListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.SessionTrackingMode;
import jakarta.servlet.descriptor.JspConfigDescriptor;
import jakarta.servlet.descriptor.JspPropertyGroupDescriptor;
import jakarta.servlet.descriptor.TaglibDescriptor;

@SuppressWarnings({ "unchecked", "deprecation" })
class OldServletContextWrapper implements ServletContext {
	private static final String UNAVAILABLE_MESSAGE = "Unable to call method on Servlet 2.5 delegate"; //$NON-NLS-1$
	final javax.servlet.ServletContext delegate;
	private final String contextPath;
	private int majorVersion = 2;
	private int minorVersion = 5;
	private ClassLoader classLoader;

	public OldServletContextWrapper(final String contextPath, final javax.servlet.ServletContext delegate) {
		this.delegate = delegate;
		this.contextPath = contextPath;
		this.classLoader = AccessController.doPrivileged((PrivilegedAction<ClassLoader>)() -> Thread.currentThread().getContextClassLoader());
	}

	public OldServletContextWrapper(final String contextPath, final javax.servlet.ServletContext delegate, final int majorVersion, final int minorVersion) {
		this(contextPath, delegate);
		this.majorVersion = majorVersion;
		this.minorVersion = minorVersion;
	}

	<T extends EventListener> List<T> getListeners(final Class<?> listenerClass) {
		List<T> result = new ArrayList<>();
		for(Object listener : getOtherListeners()) {
			if(listenerClass.isInstance(listener)) {
				result.add((T)listener);
			}
		}
		return result;
	}

	@Override
	public Dynamic addFilter(final String arg0, final String arg1) {
		throw unavailable();
	}

	@Override
	public Dynamic addFilter(final String arg0, final Filter arg1) {
		throw unavailable();
	}

	@Override
	public Dynamic addFilter(final String arg0, final Class<? extends Filter> arg1) {
		throw unavailable();
	}

	@Override
	public jakarta.servlet.ServletRegistration.Dynamic addJspFile(final String arg0, final String arg1) {
		throw unavailable();
	}

	@Override
	public void addListener(final String arg0) {
		throw unavailable();
	}

	@Override
	public <T extends EventListener> void addListener(final T listener) {
		Collection<EventListener> listeners = getOtherListeners();
		if(!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	@Override
	public void addListener(final Class<? extends EventListener> c) {
		try {
			// TODO bind this with CDI
			getOtherListeners().add(c.newInstance());
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public jakarta.servlet.ServletRegistration.Dynamic addServlet(final String arg0, final String arg1) {
		throw unavailable();
	}

	@Override
	public jakarta.servlet.ServletRegistration.Dynamic addServlet(final String arg0, final Servlet arg1) {
		throw unavailable();
	}

	@Override
	public jakarta.servlet.ServletRegistration.Dynamic addServlet(final String arg0, final Class<? extends Servlet> arg1) {
		throw unavailable();
	}

	@Override
	public <T extends Filter> T createFilter(final Class<T> arg0) throws ServletException {
		throw unavailable();
	}

	@Override
	public <T extends EventListener> T createListener(final Class<T> arg0) throws ServletException {
		throw unavailable();
	}

	@Override
	public <T extends Servlet> T createServlet(final Class<T> arg0) throws ServletException {
		throw unavailable();
	}

	@Override
	public void declareRoles(final String... arg0) {
		throw unavailable();
	}

	@Override
	public Object getAttribute(final String arg0) {
		// Handle a common case of requesting the spec-defined temp directory
		if(ServletContext.TEMPDIR.equals(arg0)) {
			Object explicit = delegate.getAttribute(arg0);
			if(explicit == null) {
				return delegate.getAttribute("javax.servlet.context.tempdir"); //$NON-NLS-1$
			}
		}
		return delegate.getAttribute(arg0);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return delegate.getAttributeNames();
	}

	@Override
	public ClassLoader getClassLoader() {
		return this.classLoader;
	}

	@Override
	public ServletContext getContext(final String arg0) {
		return ServletUtil.oldToNew(contextPath, delegate.getContext(arg0));
	}

	@Override
	public String getContextPath() {
		return Objects.requireNonNull(contextPath, "Context path requested but not initialized");
	}

	@Override
	public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
		return EnumSet.of(SessionTrackingMode.COOKIE);
	}

	@Override
	public int getEffectiveMajorVersion() {
		return 2;
	}

	@Override
	public int getEffectiveMinorVersion() {
		return 5;
	}

	@Override
	public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
		return EnumSet.of(SessionTrackingMode.COOKIE);
	}

	@Override
	public FilterRegistration getFilterRegistration(final String arg0) {
		// Soft unavailable
		return null;
	}

	@Override
	public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
		// Soft unavailable
		return Collections.emptyMap();
	}

	@Override
	public String getInitParameter(final String name) {
		Map<String, String> params = getExtraInitParameters();
		if(params.containsKey(name)) {
			return params.get(name);
		}
		return delegate.getInitParameter(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		Map<String, String> params = getExtraInitParameters();
		Set<String> paramNames = new HashSet<>();
		paramNames.addAll(Collections.list(delegate.getInitParameterNames()));
		paramNames.addAll(params.keySet());
		return Collections.enumeration(paramNames);
	}

	@Override
	public JspConfigDescriptor getJspConfigDescriptor() {
		return new JspConfigDescriptor() {

			@Override
			public Collection<TaglibDescriptor> getTaglibs() {
				return Collections.emptyList();
			}

			@Override
			public Collection<JspPropertyGroupDescriptor> getJspPropertyGroups() {
				return Collections.emptyList();
			}

		};
	}

	@Override
	public int getMajorVersion() {
		return this.majorVersion;
	}

	@Override
	public String getMimeType(final String arg0) {
		return delegate.getMimeType(arg0);
	}

	@Override
	public int getMinorVersion() {
		return this.minorVersion;
	}

	@Override
	public RequestDispatcher getNamedDispatcher(final String name) {
		// Unsupported on Domino, so try to replicate the behavior from the ComponentModule
		// TODO consider implementing. This would require the RequestDispatcher to create
		//   the Servlet anew for each request, parsing out the context and path
//		MappingBasedServletFactory factory = getServletFactories()
//			.stream()
//			.filter(MappingBasedServletFactory.class::isInstance)
//			.filter(fac -> fac.getClass().getName().equals(name))
//			.map(MappingBasedServletFactory.class::cast)
//			.filter(Objects::nonNull)
//			.findFirst()
//			.orElse(null);
//		if(factory != null) {
//			// TODO figure out if this behavior should change
//			return new RequestDispatcher() {
//
//				@Override
//				public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
//					Servlet servlet = ServletUtil.oldToNew(match.getServlet());
//					servlet.service(request, response);
//				}
//
//				@Override
//				public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
//					Servlet servlet = ServletUtil.oldToNew(match.getServlet());
//					servlet.service(request, response);
//				}
//			};
//		}
		return null;
	}

	@Override
	public String getRealPath(final String arg0) {
		return delegate.getRealPath(arg0);
	}

	@Override
	public String getRequestCharacterEncoding() {
		// TODO not assume? This could come from checking headers
		return "UTF-8"; //$NON-NLS-1$
	}

	@Override
	public RequestDispatcher getRequestDispatcher(final String path) {
		// Unsupported on Domino, so try to replicate the behavior from the ComponentModule
		ServletMatch match = getServletFactories()
			.stream()
			.map(f -> {
				try {
					return f.getServletMatch(getContextPath(), path);
				} catch (javax.servlet.ServletException e) {
					throw new RuntimeException(e);
				}
			})
			.filter(Objects::nonNull)
			.findFirst()
			.orElse(null);
		if(match != null) {
			// TODO figure out if this behavior should change
			return new RequestDispatcher() {

				@Override
				public void include(final ServletRequest request, final ServletResponse response) throws ServletException, IOException {
					Servlet servlet = ServletUtil.oldToNew(match.getServlet());
					servlet.service(request, response);
				}

				@Override
				public void forward(final ServletRequest request, final ServletResponse response) throws ServletException, IOException {
					Servlet servlet = ServletUtil.oldToNew(match.getServlet());
					servlet.service(request, response);
				}
			};
		}
		return null;
	}

	@Override
	public URL getResource(final String path) throws MalformedURLException {
		if("/WEB-INF/faces-config.xml".equals(path)) { //$NON-NLS-1$
			URL alternative = delegate.getResource("/WEB-INF/jakarta/faces-config.xml"); //$NON-NLS-1$
			if(alternative != null) {
				return alternative;
			}
		}
		return delegate.getResource(path);
	}

	@Override
	public InputStream getResourceAsStream(final String path) {
		if("/WEB-INF/faces-config.xml".equals(path)) { //$NON-NLS-1$
			InputStream alternative = delegate.getResourceAsStream("/WEB-INF/jakarta/faces-config.xml"); //$NON-NLS-1$
			if(alternative != null) {
				return alternative;
			}
		}
		return delegate.getResourceAsStream(path);
	}

	@Override
	public Set<String> getResourcePaths(final String path) {
		return delegate.getResourcePaths(path);
	}

	@Override
	public String getResponseCharacterEncoding() {
		return "UTF-8"; //$NON-NLS-1$
	}

	@Override
	public String getServerInfo() {
		return delegate.getServerInfo();
	}

	@Override
	public String getServletContextName() {
		return delegate.getServletContextName();
	}

	@Override
	public ServletRegistration getServletRegistration(final String arg0) {
		// Soft unavailable
		return null;
	}

	@Override
	public Map<String, ? extends ServletRegistration> getServletRegistrations() {
		// Not supported on Domino, so build a map based on IServletFactory instances
		return getServletFactories()
			.stream()
			.filter(MappingBasedServletFactory.class::isInstance)
			.map(MappingBasedServletFactory.class::cast)
			.collect(Collectors.toMap(
				fac -> fac.getClass().getName(),
				fac -> new ServletRegistration() {

					@Override
					public String getClassName() {
						return fac.getServletClassName();
					}

					@Override
					public String getInitParameter(final String param) {
						return null;
					}

					@Override
					public Map<String, String> getInitParameters() {
						return Collections.emptyMap();
					}

					@Override
					public String getName() {
						return fac.getClass().getName();
					}

					@Override
					public boolean setInitParameter(final String param, final String value) {
						// NOP
						return false;
					}

					@Override
					public Set<String> setInitParameters(final Map<String, String> params) {
						// NOP
						return Collections.emptySet();
					}

					@Override
					public Set<String> addMapping(final String... urlPatterns) {
						// NOP
						return fac.getExtensions();
					}

					@Override
					public Collection<String> getMappings() {
						return fac.getExtensions()
							.stream()
							.map(ext -> "*" + ext) //$NON-NLS-1$
							.collect(Collectors.toSet());
					}

					@Override
					public String getRunAsRole() {
						return null;
					}

				}
			));
	}

	@Override
	public SessionCookieConfig getSessionCookieConfig() {
		return new DummySessionCookieConfig();
	}

	@Override
	public int getSessionTimeout() {
		// Soft unavailable
		return 30;
	}

	@Override
	public String getVirtualServerName() {
		// Soft unavailable
		return ""; //$NON-NLS-1$
	}

	@Override
	public void log(final String arg0) {
		delegate.log(arg0);
	}

	@Override
	public void log(final String arg0, final Throwable arg1) {
		delegate.log(arg0, arg1);
	}

	@Override
	public void removeAttribute(final String name) {
		Object val = delegate.getAttribute(name);
		delegate.removeAttribute(name);
		this.getAttrListeners().forEach(listener ->
			listener.attributeRemoved(new ServletContextAttributeEvent(this, name, val))
		);
	}

	@Override
	public void setAttribute(final String name, final Object value) {
		boolean exists = Collections.list(this.getAttributeNames()).contains(name);
		Object oldVal = delegate.getAttribute(name);
		delegate.setAttribute(name, value);
		if(exists) {
			this.getAttrListeners().forEach(listener ->
				listener.attributeReplaced(new ServletContextAttributeEvent(this, name, oldVal))
			);
		}
		this.getAttrListeners().forEach(listener ->
			listener.attributeAdded(new ServletContextAttributeEvent( this, name, value))
		);
	}

	@Override
	public boolean setInitParameter(final String name, final String value) {
		if(Collections.list(getInitParameterNames()).contains(name)) {
			return false;
		}
		getExtraInitParameters().put(name, value);
		return true;
	}

	@Override
	public void setRequestCharacterEncoding(final String arg0) {
		throw unavailable();
	}

	@Override
	public void setResponseCharacterEncoding(final String arg0) {
		throw unavailable();
	}

	@Override
	public void setSessionTimeout(final int arg0) {
		// Soft unavailable
	}

	@Override
	public void setSessionTrackingModes(final Set<SessionTrackingMode> arg0) {
		// Soft unavailable
	}

	// *******************************************************************************
	// * Internal utility methods
	// *******************************************************************************

	private RuntimeException unavailable() {
		return new UnsupportedOperationException(UNAVAILABLE_MESSAGE);
	}

	private Collection<? extends IServletFactory> getServletFactories() {
		return ComponentModuleLocator.getDefault()
			.map(ComponentModuleLocator::getServletFactories)
			.orElseGet(Collections::emptyList);
	}

	private static final String ATTR_LISTENERS = OldServletContextWrapper.class.getName() + "_listeners"; //$NON-NLS-1$
	private static final String ATTR_INITPARAMS = OldServletContextWrapper.class.getName() + "_initParams"; //$NON-NLS-1$

	private Stream<ServletContextAttributeListener> getAttrListeners() {
		return getOtherListeners()
			.stream()
			.filter(ServletContextAttributeListener.class::isInstance)
			.map(ServletContextAttributeListener.class::cast);
	}

	private List<EventListener> getOtherListeners() {
		List<EventListener> result = (List<EventListener>)delegate.getAttribute(ATTR_LISTENERS);
		if(result == null) {
			result = new ArrayList<>();
			delegate.setAttribute(ATTR_LISTENERS, result);
		}
		return result;
	}

	Map<String, String> getExtraInitParameters() {
		Map<String, String> result = (Map<String, String>)delegate.getAttribute(ATTR_INITPARAMS);
		if(result == null) {
			result = new HashMap<>();
			delegate.setAttribute(ATTR_INITPARAMS, result);
		}
		return result;
	}
}
