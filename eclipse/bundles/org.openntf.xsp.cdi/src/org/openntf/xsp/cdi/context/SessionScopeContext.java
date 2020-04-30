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
	protected BasicScopeContextHolder getHolder() {
		HttpServletRequest req = getHttpServletRequest();
		HttpSession session = req.getSession();
		String key = generateKey();
		
		BasicScopeContextHolder holder = (BasicScopeContextHolder)session.getAttribute(key);
		if(holder == null) {
			holder = new BasicScopeContextHolder();
			session.setAttribute(key, holder);
		}
		return holder;
	}
}
