package org.openntf.xsp.jakartaee.servlet;

/**
 * This utility class contains methods for converting between old
 * and new Servlet API classes.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public enum ServletUtil {
	;
	// TODO check for null
	// TODO check for unwrap
	
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
		if(req == null) {
			return null;
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
	public static jakarta.servlet.ServletContext oldToNew(javax.servlet.ServletContext context) {
		if(context == null) {
			return null;
		} else if(context instanceof NewServletContextWrapper) {
			return ((NewServletContextWrapper)context).delegate;
		} else {
			return new OldServletContextWrapper(context);
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
	
}
