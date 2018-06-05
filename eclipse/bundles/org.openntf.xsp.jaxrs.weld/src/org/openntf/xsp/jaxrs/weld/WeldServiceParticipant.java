package org.openntf.xsp.jaxrs.weld;

import java.io.IOException;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.glassfish.jersey.process.internal.RequestScoped;
import org.jboss.weld.manager.BeanManagerImpl;
import org.openntf.xsp.cdi.util.ContainerUtil;
import org.openntf.xsp.jaxrs.ServiceParticipant;

import com.ibm.xsp.application.ApplicationEx;

public class WeldServiceParticipant implements ServiceParticipant {
	private static final String CACHE_KEY = WeldServiceParticipant.class.getName();

	@Override
	public void doBeforeService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		ApplicationEx application = ApplicationEx.getInstance();
		BeanManagerImpl manager = ContainerUtil.getBeanManager(application);
		if(!manager.isContextActive(RequestScoped.class)) {
			// Build up the request context
			@SuppressWarnings("unchecked")
			Map<String, Object> requestScope = FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
			manager.addContext((JerseyRequestScopedContext)requestScope.compute(CACHE_KEY, (key, val) -> new JerseyRequestScopedContext(manager.getContextId())));
		}
	}

	@Override
	public void doAfterService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Tear down the request context
		@SuppressWarnings("unchecked")
		Map<String, Object> requestScope = FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
		JerseyRequestScopedContext context = (JerseyRequestScopedContext)requestScope.get(CACHE_KEY);
		if(context != null) {
			context.invalidate();
		}
	}

}
