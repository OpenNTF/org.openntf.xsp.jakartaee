package org.openntf.xsp.el3.impl;

import javax.el.BeanNameResolver;
import javax.faces.context.FacesContext;
import javax.faces.el.VariableResolver;

/**
 * Provides bean resolution in a Faces context.
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class FacesBeanNameResolver extends BeanNameResolver {

	@Override
	public boolean isNameResolved(String beanName) {
		return getBean(beanName) != null;
	}
	
	@Override
	public Object getBean(String beanName) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		VariableResolver vr = facesContext.getApplication().getVariableResolver();
		return vr.resolveVariable(facesContext, beanName);
	}

}
