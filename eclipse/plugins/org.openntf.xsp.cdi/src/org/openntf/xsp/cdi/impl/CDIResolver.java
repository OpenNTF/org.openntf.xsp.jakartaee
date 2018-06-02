package org.openntf.xsp.cdi.impl;

import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.VariableResolver;

import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.inject.WeldInstance;
import org.jboss.weld.literal.NamedLiteral;

import com.ibm.xsp.application.ApplicationEx;

public class CDIResolver extends VariableResolver {

	private final VariableResolver delegate;
	
	public CDIResolver(VariableResolver delegate) {
		this.delegate = delegate;
	}

	@Override
	public Object resolveVariable(FacesContext facesContext, String name) throws EvaluationException {
		if(name == null) {
			return null;
		}
		
		// Check the delegate first, since this adds the beans to the appropriate scope as needed
		if(delegate != null) {
			Object existing = delegate.resolveVariable(facesContext, name);
			if(existing != null) {
				return existing;
			}
		}
		
		// Finally, ask CDI for a named bean
		WeldContainer container = WeldApplicationListener.getContainer(ApplicationEx.getInstance(facesContext));
		WeldInstance<Object> instance = container.select(new NamedLiteral(name));
		if(instance.isResolvable()) {
			return instance.get();
		}
		
		return null;
	}

}
