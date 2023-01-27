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
package org.openntf.xsp.jakartaee.servlet;

import java.io.IOException;
import java.util.Collections;
import java.util.EventListener;
import java.util.List;

import org.openntf.xsp.jakartaee.util.LibraryUtil;

import jakarta.servlet.ServletContextAttributeListener;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletRequestAttributeListener;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSessionAttributeListener;

/**
 * This utility class contains methods for converting between old
 * and new Servlet API classes.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public enum ServletUtil {
	;
	
	public static javax.servlet.Servlet newToOld(jakarta.servlet.Servlet servlet) {
		if(servlet == null) {
			return null;
		} else if(servlet instanceof OldHttpServletWrapper) {
			return ((OldHttpServletWrapper)servlet).delegate;
		} else {
			return new NewHttpServletWrapper(servlet);
		}
	}
	public static jakarta.servlet.Servlet oldToNew(javax.servlet.Servlet servlet) {
		if(servlet == null) {
			return null;
		} else if(servlet instanceof NewHttpServletWrapper) {
			return ((NewHttpServletWrapper)servlet).delegate;
		} else {
			return new OldHttpServletWrapper(servlet);
		}
	}
	
	public static javax.servlet.http.HttpServletRequest newToOld(jakarta.servlet.http.HttpServletRequest req) {
		return newToOld(req, false);
	}
	/**
	 * Variant of {@link #newToOld(jakarta.servlet.http.HttpServletRequest)} that allows for the creation
	 * of a wrapper that hides the request's {@code InputStream} and {@code BufferedReader} in the wrapped
	 * version.
	 * 
	 * @param req the Jakarta request to wrap
	 * @param hideBody whether to hide the body content from the wrapper
	 * @return a Java EE wrapper for the request
	 * @since 2.10.0
	 */
	public static javax.servlet.http.HttpServletRequest newToOld(jakarta.servlet.http.HttpServletRequest req, boolean hideBody) {
		if(req == null) {
			return null;
		} else if(hideBody) {
			return new HiddenBodyHttpServletRequestWrapper(req);
		} else if(req instanceof OldHttpServletRequestWrapper) {
			return ((OldHttpServletRequestWrapper)req).delegate;
		} else {
			return new NewHttpServletRequestWrapper(req);
		}
	}
	public static jakarta.servlet.http.HttpServletRequest oldToNew(javax.servlet.ServletContext context, javax.servlet.http.HttpServletRequest request) {
		if(request == null) {
			return null;
		} else if(request instanceof NewHttpServletRequestWrapper) {
			return ((NewHttpServletRequestWrapper)request).delegate;
		} else {
			return new OldHttpServletRequestWrapper(context, request);
		}
	}
	
	public static javax.servlet.http.HttpServletResponse newToOld(jakarta.servlet.http.HttpServletResponse resp) {
		if(resp == null) {
			return null;
		} else if(resp instanceof OldHttpServletResponseWrapper) {
			return ((OldHttpServletResponseWrapper)resp).delegate;
		} else {
			return new NewHttpServletResponseWrapper(resp);
		}
	}
	public static jakarta.servlet.http.HttpServletResponse oldToNew(javax.servlet.http.HttpServletResponse resp) {
		if(resp == null) {
			return null;
		} else if(resp instanceof NewHttpServletResponseWrapper) {
			return ((NewHttpServletResponseWrapper)resp).delegate;
		} else {
			return new OldHttpServletResponseWrapper(resp);
		}
	}
	
	@SuppressWarnings("deprecation")
	public static javax.servlet.http.HttpSessionContext newToOld(jakarta.servlet.http.HttpSessionContext context) {
		if(context == null) {
			return null;
		} else if(context instanceof OldHttpSessionContextWrapper) {
			return ((OldHttpSessionContextWrapper)context).delegate;
		} else {
			return new NewHttpSessionContextWrapper(context);
		}
	}
	@SuppressWarnings("deprecation")
	public static jakarta.servlet.http.HttpSessionContext oldToNew(javax.servlet.http.HttpSessionContext context) {
		if(context == null) {
			return null;
		} else if(context instanceof NewHttpSessionContextWrapper) {
			return ((NewHttpSessionContextWrapper)context).delegate;
		} else {
			return new OldHttpSessionContextWrapper(context);
		}
	}
	
	public static javax.servlet.http.HttpSession newToOld(jakarta.servlet.http.HttpSession session) {
		if(session == null) {
			return null;
		} else if(session instanceof OldHttpSessionWrapper) {
			return ((OldHttpSessionWrapper)session).delegate;
		} else {
			return new NewHttpSessionWrapper(session);
		}
	}
	public static jakarta.servlet.http.HttpSession oldToNew(javax.servlet.http.HttpSession session) {
		if(session == null) {
			return null;
		} else if(session instanceof NewHttpSessionWrapper) {
			return ((NewHttpSessionWrapper)session).delegate;
		} else {
			return new OldHttpSessionWrapper(session);
		}
	}
	
	public static javax.servlet.RequestDispatcher newToOld(jakarta.servlet.RequestDispatcher disp) {
		if(disp == null) {
			return null;
		} else if(disp instanceof OldRequestDispatcherWrapper) {
			return ((OldRequestDispatcherWrapper)disp).delegate;
		} else {
			return new NewRequestDispatcherWrapper(disp);
		}
	}
	public static jakarta.servlet.RequestDispatcher oldToNew(javax.servlet.RequestDispatcher disp) {
		if(disp == null) {
			return null;
		} else if(disp instanceof NewRequestDispatcherWrapper) {
			return ((NewRequestDispatcherWrapper)disp).delegate;
		} else {
			return new OldRequestDispatcherWrapper(disp);
		}
	}
	
	public static javax.servlet.ServletConfig newToOld(jakarta.servlet.ServletConfig config) {
		if(config == null) {
			return null;
		} else if(config instanceof OldServletConfigWrapper) {
			return ((OldServletConfigWrapper)config).delegate;
		} else {
			return new NewServletConfigWrapper(config);
		}
	}
	public static jakarta.servlet.ServletConfig oldToNew(javax.servlet.ServletConfig config) {
		if(config == null) {
			return null;
		} else if(config instanceof NewServletConfigWrapper) {
			return ((NewServletConfigWrapper)config).delegate;
		} else {
			return new OldServletConfigWrapper(config);
		}
	}
	
	public static javax.servlet.ServletContext newToOld(jakarta.servlet.ServletContext context) {
		if(context == null) {
			return null;
		} else if(context instanceof OldServletContextWrapper) {
			return ((OldServletContextWrapper)context).delegate;
		} else {
			return new NewServletContextWrapper(context);
		}
	}
	public static jakarta.servlet.ServletContext oldToNew(String contextPath, javax.servlet.ServletContext context) {
		if(context == null) {
			return null;
		} else if(context instanceof NewServletContextWrapper) {
			return ((NewServletContextWrapper)context).delegate;
		} else {
			return new OldServletContextWrapper(contextPath, context);
		}
	}
	/**
	 * Wraps the provided {@link javax.servlet.ServletContext} implementation in a
	 * {@link jakarta.servlet.ServletContext} wrapper.
	 * 
	 * <p>This method allows specification of the effective Servlet version, overriding the default
	 * 2.5.</p>
	 * 
	 * @param contextPath the context path of the servet, or {@code null} if unavailable
	 * @param context the {@link javax.servlet.ServletContext} to wrap
	 * @param majorVersion the effective major version to set
	 * @param minorVersion the effective minor version to set
	 * @return a {@link jakarta.servlet.ServletContext} wrapper
	 * @since 2.3.0
	 */
	public static jakarta.servlet.ServletContext oldToNew(String contextPath, javax.servlet.ServletContext context, int majorVersion, int minorVersion) {
		if(context == null) {
			return null;
		} else if(context instanceof NewServletContextWrapper) {
			return ((NewServletContextWrapper)context).delegate;
		} else {
			return new OldServletContextWrapper(contextPath, context, majorVersion, minorVersion);
		}
	}
	
	public static javax.servlet.ServletInputStream newToOld(jakarta.servlet.ServletInputStream is) {
		if(is == null) {
			return null;
		} else if(is instanceof OldServletInputStreamWrapper) {
			return ((OldServletInputStreamWrapper)is).delegate;
		} else {
			return new NewServletInputStreamWrapper(is);
		}
	}
	public static jakarta.servlet.ServletInputStream oldToNew(javax.servlet.ServletInputStream is) {
		if(is == null) {
			return null;
		} else if(is instanceof NewServletInputStreamWrapper) {
			return ((NewServletInputStreamWrapper)is).delegate;
		} else {
			return new OldServletInputStreamWrapper(is);
		}
	}
	
	public static javax.servlet.ServletOutputStream newToOld(jakarta.servlet.ServletOutputStream os) {
		if(os == null) {
			return null;
		} else if(os instanceof OldServletOutputStreamWrapper) {
			return ((OldServletOutputStreamWrapper)os).delegate;
		} else {
			return new NewServletOutputStreamWrapper(os);
		}
	}
	public static jakarta.servlet.ServletOutputStream oldToNew(javax.servlet.ServletOutputStream os) {
		if(os == null) {
			return null;
		} else if(os instanceof NewServletOutputStreamWrapper) {
			return ((NewServletOutputStreamWrapper)os).delegate;
		} else {
			return new OldServletOutputStreamWrapper(os);
		}
	}
	
	public static javax.servlet.http.Cookie newToOld(jakarta.servlet.http.Cookie cookie) {
		if(cookie == null) {
			return null;
		} else if(cookie instanceof OldCookieWrapper) {
			return ((OldCookieWrapper)cookie).delegate;
		} else {
			return new NewCookieWrapper(cookie);
		}
	}
	public static jakarta.servlet.http.Cookie oldToNew(javax.servlet.http.Cookie cookie) {
		if(cookie == null) {
			return null;
		} else if(cookie instanceof NewCookieWrapper) {
			return ((NewCookieWrapper)cookie).delegate;
		} else {
			return new OldCookieWrapper(cookie);
		}
	}
	
	/**
	 * Wraps an old {@code ServletException} with a new one.
	 * 
	 * @param e the old-style exception to wrap
	 * @return a new-style exception
	 * @since 2.8.0
	 */
	public static jakarta.servlet.ServletException oldToNew(javax.servlet.ServletException e) {
		return new jakarta.servlet.ServletException(e);
	}
	/**
	 * Wraps a new {@code ServletException} with an old one.
	 * 
	 * @param e the new-style exception to wrap
	 * @return an old-style exception
	 * @since 2.8.0
	 */
	public static javax.servlet.ServletException newToOld(jakarta.servlet.ServletException e) {
		return new javax.servlet.ServletException(e);
	}
	
	// *******************************************************************************
	// * Shim methods for working with listeners
	// *******************************************************************************
	
	public static void addListener(jakarta.servlet.ServletRequest req, ServletRequestAttributeListener listener) {
		if(req == null) {
			return;
		}
		if(!(req instanceof OldHttpServletRequestWrapper)) {
			throw new IllegalArgumentException("req is not an instance of " + OldHttpServletRequestWrapper.class.getName());
		}
		((OldHttpServletRequestWrapper)req).addListener(listener);
	}
	
	public static void addListener(jakarta.servlet.http.HttpSession session, HttpSessionAttributeListener listener) {
		if(session == null) {
			return;
		}
		if(!(session instanceof OldHttpSessionWrapper)) {
			throw new IllegalArgumentException("session is not an instance of " + OldHttpSessionWrapper.class.getName());
		}
		((OldHttpSessionWrapper)session).addListener(listener);
	}

	public static void addListener(jakarta.servlet.ServletContext context, ServletContextAttributeListener listener) {
		if(context == null) {
			return;
		}
		if(!(context instanceof OldServletContextWrapper)) {
			throw new IllegalArgumentException("context is not an instance of " + OldServletContextWrapper.class.getName());
		}
		((OldServletContextWrapper)context).addListener(listener);
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends EventListener> List<T> getListeners(jakarta.servlet.ServletContext context, Class<T> listenerClass) {
		if(context == null) {
			return Collections.emptyList();
		}
		if(!(context instanceof OldServletContextWrapper)) {
			throw new IllegalArgumentException("context is not an instance of " + OldServletContextWrapper.class.getName());
		}
		return (List<T>)((OldServletContextWrapper)context).getListeners(listenerClass);
	}
	
	private static final String ATTR_CONTEXTINITIALIZED = ServletUtil.class.getName() + "_contextInitialized"; //$NON-NLS-1$
	
	public static void contextInitialized(jakarta.servlet.ServletContext context) {
		if(!Boolean.TRUE.equals(context.getAttribute(ATTR_CONTEXTINITIALIZED))) {
			LibraryUtil.findExtensionsUncached(ServletContextListener.class)
				.forEach(l -> context.addListener(l));
			
			getListeners(context, ServletContextListener.class)
				.forEach(l -> l.contextInitialized(new ServletContextEvent(context)));
			
			context.setAttribute(ATTR_CONTEXTINITIALIZED, Boolean.TRUE);
		}
	}
	
	/**
	 * Attempts to close the writer or stream associated with this response.
	 * 
	 * <p>This is intended for use with Servlet delegates that may not reliably
	 * themselves flush the buffer.</p>
	 * 
	 * @param resp the response to close
	 * @since 2.9.0 
	 */
	public static void close(HttpServletResponse resp) {
		// NB: resp.flushBuffer() is insufficient here
		try {
			resp.getWriter().flush();
		} catch(IllegalStateException e) {
			// Written using the stream instead
			try {
				resp.getOutputStream().flush();
			} catch(IllegalStateException e2) {
				// Well, fine.
			} catch(IOException e2) {
				// Is "ServletOutputStream is closed" when serving resources
				// Either way, nothing to do with it here
			}
		} catch(IOException e) {
			// No need to propagate this
		}
		try {
			resp.flushBuffer();
		} catch (IOException e) {
			// No need to propagate this
		}
	}
}
