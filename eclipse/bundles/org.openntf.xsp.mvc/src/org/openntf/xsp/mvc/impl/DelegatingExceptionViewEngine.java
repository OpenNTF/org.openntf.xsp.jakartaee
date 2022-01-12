package org.openntf.xsp.mvc.impl;

import java.io.IOException;

import jakarta.mvc.Models;
import jakarta.mvc.engine.ViewEngine;
import jakarta.mvc.engine.ViewEngineContext;
import jakarta.mvc.engine.ViewEngineException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

/**
 * @author Jesse Gallagher
 * @since 2.3.0
 */
public class DelegatingExceptionViewEngine implements ViewEngine {
	@Override
	public boolean supports(String view) {
		return getClass().getName().equals(view);
	}

	@Override
	public void processView(ViewEngineContext context) throws ViewEngineException {
		Models models = context.getModels();
		Response sup = (Response)models.get("response"); //$NON-NLS-1$
		Object entity = sup.getEntity();
		if(entity instanceof StreamingOutput) {
			try {
				((StreamingOutput)entity).write(context.getOutputStream());
			} catch (WebApplicationException | IOException e) {
				throw new ViewEngineException(e);
			}
		} else {
			throw new UnsupportedOperationException("Unexpected entity type: " + (entity == null ? "null" : entity.getClass().getName())); //$NON-NLS-2$
		}
	}

}
