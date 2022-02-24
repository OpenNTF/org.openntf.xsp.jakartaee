package org.openntf.xsp.jakarta.servlet.nsf;

import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.ServletException;

import jakarta.servlet.Servlet;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.annotation.WebServlet;

import org.openntf.xsp.jakarta.servlet.ServletLibrary;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.jakartaee.util.ModuleUtil;

import com.ibm.commons.util.PathUtil;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.IServletFactory;
import com.ibm.designer.runtime.domino.adapter.ServletMatch;

/**
 * Provides support for Servlet classes inside an NSF annotated with {@link WebServlet}.
 * 
 * @author Jesse Gallagher
 * @since 2.5.0
 */
public class ServletServletFactory implements IServletFactory {
	private ComponentModule module;
	private Map<WebServlet, Class<? extends Servlet>> servletClasses;
	private Map<Class<? extends Servlet>, javax.servlet.Servlet> servlets;
	private long lastUpdate;

	@Override
	public void init(ComponentModule module) {
		this.module = module;
	}

	@Override
	public final ServletMatch getServletMatch(String contextPath, String path) throws javax.servlet.ServletException {
		try {
			if(LibraryUtil.usesLibrary(ServletLibrary.LIBRARY_ID, module)) {
				for(Map.Entry<WebServlet, Class<? extends Servlet>> entry : getModuleServlets().entrySet()) {
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
		}
		return null;
	}
	
	private String matches(WebServlet mapping, String path) {
		// Context path is like /some/db.nsf
		// Path is like /xsp/someservlet (no query string)
		
		if(path == null || path.length() < 5) {
			return null;
		}
		
		String[] patterns = mapping.value();
		if(patterns == null || patterns.length == 0) {
			patterns = mapping.urlPatterns();
		}
		
		if(patterns != null) {
			for(String pattern : patterns) {
				if(pattern != null) {
					if("/".equals(pattern) || "/*".equals(pattern)) { //$NON-NLS-1$ //$NON-NLS-2$
						// Ignore for now, since it could butt heads with the runtime
						continue;
					} else if(pattern.endsWith("/*")) { //$NON-NLS-1$
						// Path-matching pattern
						String prefix = pattern.substring(0, pattern.length()-2);
						String effectivePrefix = PathUtil.concat("/xsp", prefix, '/'); //$NON-NLS-1$
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
						String effectivePath = PathUtil.concat("/xsp", pattern, '/'); //$NON-NLS-1$
						if(path.equals(effectivePath)) {
							return pattern;
						}
					}
				}
			}
		}
		
		return null;
	}
	
	private String findPathInfo(String path, String pattern) {
		if(pattern.endsWith("/*")) { //$NON-NLS-1$
			// Path-matching pattern
			String prefix = pattern.substring(0, pattern.length()-2);
			String effectivePrefix = PathUtil.concat("/xsp", prefix, '/'); //$NON-NLS-1$
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
	
	private javax.servlet.Servlet getExecutorServlet(WebServlet mapping, Class<? extends Servlet> c) {
		checkInvalidate();
		return this.servlets.computeIfAbsent(c, key -> {
			try {
				Servlet delegate = c.newInstance();
				Servlet wrapper = new XspServletWrapper(module, delegate);
				
				Map<String, String> params = Arrays.stream(mapping.initParams())
					.collect(Collectors.toMap(
						WebInitParam::name,
						WebInitParam::value
					));
				
				return module.createServlet(ServletUtil.newToOld(wrapper), mapping.name(), params);
			} catch (InstantiationException | IllegalAccessException | ServletException e) {
				throw new RuntimeException(e);
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	private synchronized Map<WebServlet, Class<? extends Servlet>> getModuleServlets() {
		checkInvalidate();
		
		if(this.servletClasses == null) {
			this.servletClasses = ModuleUtil.getClasses(this.module)
				.filter(c -> c.isAnnotationPresent(WebServlet.class))
				.filter(Servlet.class::isAssignableFrom)
				.collect(Collectors.toMap(
					c -> c.getAnnotation(WebServlet.class),
					c -> (Class<? extends Servlet>)c
				));
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

}
