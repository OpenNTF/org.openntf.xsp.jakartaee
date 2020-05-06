package org.openntf.xsp.cdi.context;

import java.lang.annotation.Annotation;

import javax.enterprise.context.SessionScoped;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author Jesse Gallagher
 * @since 1.2.0
 */
public class SessionScopeContext extends AbstractProxyingContext {
	private static final long serialVersionUID = 1L;

	@Override
	public Class<? extends Annotation> getScope() {
		return SessionScoped.class;
	}
	
	@Override
	protected synchronized BasicScopeContextHolder getHolder() {
		HttpServletRequest req = getHttpServletRequest();
		if(req != null) {
			HttpSession session = req.getSession(true);
			String key = generateKey();
			
			BasicScopeContextHolder holder = (BasicScopeContextHolder)session.getAttribute(key);
			if(holder == null) {
				holder = new BasicScopeContextHolder();
				session.setAttribute(key, holder);
			}
			return holder;
		} else {
			// Must be in a non-HTTP task - just spin up a discardable one
			return new BasicScopeContextHolder();
		}
	}
}
