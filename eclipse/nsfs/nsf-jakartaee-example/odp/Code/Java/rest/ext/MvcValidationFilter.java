package rest.ext;

import java.io.IOException;
import java.lang.reflect.Method;

import org.eclipse.krazo.engine.Viewable;

import jakarta.annotation.Priority;
import jakarta.mvc.Controller;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.ENTITY_CODER-1)
public class MvcValidationFilter implements ContainerResponseFilter {
	
	@Context
	private ResourceInfo resourceInfo;

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		Method m = resourceInfo.getResourceMethod();
		if(m.isAnnotationPresent(Controller.class) || m.getDeclaringClass().isAnnotationPresent(Controller.class)) {
			// Make sure the final value is a Viewable, to catch a bug observed at some points
			Object entity = responseContext.getEntity();
			if(!(entity instanceof Viewable)) {
				responseContext.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				responseContext.setEntity("Returned entity is a " + (entity == null ? null : entity.getClass()) + " and not a Viewable: " + entity);
			}
		}
	}

}
