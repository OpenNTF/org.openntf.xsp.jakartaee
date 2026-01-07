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
package org.openntf.xsp.jakarta.servlet.nsf;

import java.io.UncheckedIOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.ServletMatch;

import org.apache.tomcat.util.descriptor.web.ServletDef;
import org.apache.tomcat.util.descriptor.web.WebXml;
import org.openntf.xsp.jakarta.cdi.util.DiscoveryUtil;
import org.openntf.xsp.jakartaee.module.JakartaIServletFactory;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.jakartaee.util.ModuleUtil;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.Servlet;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;

/**
 * Provides support for Servlet classes inside an NSF annotated with {@link WebServlet}.
 *
 * @author Jesse Gallagher
 * @since 2.5.0
 */
public class ServletServletFactory implements JakartaIServletFactory {
	private static final Logger log = System.getLogger(ServletServletFactory.class.getName());

	private ComponentModule module;
	private Map<ServletInfo, Class<? extends Servlet>> servletClasses;
	private Map<Class<? extends Servlet>, javax.servlet.Servlet> servlets;
	private long lastUpdate;

	@Override
	public void init(final ComponentModule module) {
		this.module = module;
	}

	@Override
	public final ServletMatch getServletMatch(final String contextPath, final String path) throws javax.servlet.ServletException {
		try {
			if(LibraryUtil.usesLibrary(LibraryUtil.LIBRARY_CORE, module)) {
				for(Map.Entry<ServletInfo, Class<? extends Servlet>> entry : getModuleServlets().entrySet()) {
					String match = matches(entry.getKey(), path);
					if(match != null) {
						javax.servlet.Servlet servlet = getExecutorServlet(entry.getKey(), entry.getValue());
						String pathInfo = findPathInfo(path, match);
						int pathInfoLen = pathInfo == null ? 0 : pathInfo.length();
						return new ServletMatch(servlet, path.substring(0, path.length()-pathInfoLen), pathInfo);
					}
				}
			}
		} catch (UncheckedIOException e) {
			throw new javax.servlet.ServletException(e);
		} catch(Throwable t) {
			log.log(Level.ERROR, () -> MessageFormat.format("Encountered unexpected exception finding Servlet match for {0} in {1}", path, module), t);
		}
		return null;
	}

	private String matches(final ServletInfo mapping, final String path) {
		// Context path is like /some/db.nsf
		// Path is like /xsp/someservlet (no query string)

		String xspPrefix = ModuleUtil.getXspPrefix(module);
		if(path == null || path.length() < xspPrefix.length()+1) {
			return null;
		}

		List<String> patterns = mapping.patterns;

		if(patterns != null) {
			for(String pattern : patterns) {
				if(pattern != null) {
					if("/".equals(pattern) || "/*".equals(pattern)) { //$NON-NLS-1$ //$NON-NLS-2$
						// Ignore for now, since it could butt heads with the runtime
						continue;
					} else if(pattern.endsWith("/*")) { //$NON-NLS-1$
						// Path-matching pattern
						String prefix = pattern.substring(0, pattern.length()-2);
						String effectivePrefix = PathUtil.concat(xspPrefix, prefix, '/');
						if(path.startsWith(effectivePrefix)) {
							return pattern;
						}
					} else if(pattern.startsWith("*.")) { //$NON-NLS-1$
						// TODO see if this is supposed to match - it seems undefined
						Pattern p = Pattern.compile("[\\w\\d]" + Pattern.quote(pattern.substring(1)) + "(/|$)"); //$NON-NLS-1$ //$NON-NLS-2$
						if(p.matcher(path).find()) {
							return pattern;
						}
					} else {
						// Exact match pattern
						String effectivePath = PathUtil.concat(xspPrefix, pattern, '/');
						if(path.equals(effectivePath)) {
							return pattern;
						}
					}
				}
			}
		}

		return null;
	}

	private String findPathInfo(final String path, final String pattern) {
		String xspPrefix = ModuleUtil.getXspPrefix(module);
		if(pattern.endsWith("/*")) { //$NON-NLS-1$
			// Path-matching pattern
			String prefix = pattern.substring(0, pattern.length()-2);
			String effectivePrefix = PathUtil.concat(xspPrefix, prefix, '/');
			if(path.startsWith(effectivePrefix)) {
				return path.substring(effectivePrefix.length());
			}
		} else if(pattern.startsWith("*.")) { //$NON-NLS-1$
			int extIndex = path.indexOf(pattern.substring(1));
			if(extIndex > -1 && extIndex+pattern.substring(1).length() < path.length()-1) {
				return path.substring(extIndex+pattern.substring(1).length());
			}
		} else {
			// Exact match pattern
		}
		return null;
	}

	private javax.servlet.Servlet getExecutorServlet(final ServletInfo mapping, final Class<? extends Servlet> c) {
		checkInvalidate();
		return this.servlets.computeIfAbsent(c, key -> {
			try {
				Servlet delegate;
				if(Arrays.stream(c.getAnnotations()).anyMatch(DiscoveryUtil::isBeanDefining) || c.isAnnotationPresent(WebServlet.class)) {
					delegate = CDI.current().select(c).get();
				} else {
					delegate = c.getConstructor().newInstance();
				}
				if(ModuleUtil.hasXPages(module)) {
					delegate = new XspServletWrapper(module, delegate);
				} else {
					delegate = new ErrorHandlingServletWrapper(delegate);
				}

				Map<String, String> params = mapping.def.getParameterMap();

				return module.createServlet(ServletUtil.newToOld(delegate), mapping.def.getServletName(), params);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	@SuppressWarnings("unchecked")
	private synchronized Map<ServletInfo, Class<? extends Servlet>> getModuleServlets() {
		checkInvalidate();

		if(this.servletClasses == null) {
			Map<ServletInfo, Class<? extends Servlet>> result = new HashMap<>();
			ModuleUtil.getClasses(this.module)
				.filter(c -> c.isAnnotationPresent(WebServlet.class))
				.filter(Servlet.class::isAssignableFrom)
				.forEach(c -> result.put(toServletDef(c), (Class<? extends Servlet>)c));

			ClassLoader cl = module.getModuleClassLoader();
			if(cl != null) {
				WebXml webXml = ServletUtil.getWebXml(module);

				// mappings is pattern -> name, so reverse for our needs
				Map<String, String> mappings = webXml.getServletMappings();
				Map<String, List<String>> nameToPattern = new HashMap<>();
				mappings.forEach((pattern, name) -> nameToPattern.computeIfAbsent(name, k -> new ArrayList<>()).add(pattern));

				Map<String, ServletDef> defs = webXml.getServlets();
				nameToPattern.forEach((name, patterns) -> {
					ServletInfo info = new ServletInfo(defs.get(name), patterns);

					try {
						result.put(info, (Class<? extends Servlet>)cl.loadClass(info.def.getServletClass()));
					} catch (ClassNotFoundException e) {
						log.log(Level.ERROR, () -> MessageFormat.format("Encountered exception loading Servlet class {0}", info.def.getServletClass()), e);
					}
				});
			}

			this.servletClasses = result;
		}
		return this.servletClasses;
	}

	private void checkInvalidate() {
		if(lastUpdate < this.module.getLastRefresh()) {
			if(servlets != null) {
				this.servlets.forEach((c, servlet) -> servlet.destroy());
			}
			this.servlets = new HashMap<>();
			this.servletClasses = null;
		}
	}

	private ServletInfo toServletDef(final Class<?> c) {
		WebServlet annotation = c.getAnnotation(WebServlet.class);

		ServletDef def = new ServletDef();

		def.setAsyncSupported(Boolean.toString(annotation.asyncSupported()));
		def.setDescription(annotation.description());
		def.setDisplayName(annotation.displayName());
		def.setEnabled(Boolean.toString(true));
		def.setLargeIcon(annotation.largeIcon());
		def.setLoadOnStartup(Integer.toString(annotation.loadOnStartup()));
		def.setSmallIcon(annotation.smallIcon());
		def.setServletClass(c.getName());

		String name = annotation.name();
		if(StringUtil.isEmpty(name)) {
			name = c.getName();
		}
		def.setServletName(name);


		for(WebInitParam param : annotation.initParams()) {
			def.addInitParameter(param.name(), param.value());
		}

		String[] patterns = annotation.value();
		if(patterns == null || patterns.length == 0) {
			patterns = annotation.urlPatterns();
		}
		return new ServletInfo(def, List.of(patterns));
	}

	private record ServletInfo(ServletDef def, List<String> patterns) {}

}
