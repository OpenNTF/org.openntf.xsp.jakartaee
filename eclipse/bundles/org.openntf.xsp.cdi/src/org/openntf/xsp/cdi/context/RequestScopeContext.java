package org.openntf.xsp.cdi.context;

import java.lang.annotation.Annotation;

import javax.enterprise.context.RequestScoped;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Jesse Gallagher
 * @since 1.2.0
 */
public class RequestScopeContext extends AbstractProxyingContext {
	private static final long serialVersionUID = 1L;

	@Override
	public Class<? extends Annotation> getScope() {
		return RequestScoped.class;
	}
	
	@Override
	public BasicScopeContextHolder getHolder() {
		HttpServletRequest req = getHttpServletRequest();
		if(req != null) {
			String key = generateKey();
			
			BasicScopeContextHolder holder = (BasicScopeContextHolder)req.getAttribute(key);
			if(holder == null) {
				holder = new BasicScopeContextHolder();
				req.setAttribute(key, holder);
			}
			return holder;
		} else {
			// Must be in a non-HTTP task - just spin up a discardable one
			return new BasicScopeContextHolder();
		}
	}
}
