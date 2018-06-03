package org.openntf.xsp.el3.impl;

import javax.el.ELContext;
import javax.el.MethodExpression;
import javax.faces.component.StateHolder;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.MethodBinding;
import javax.faces.el.MethodNotFoundException;

import org.openntf.xsp.el3.EL3BindingFactory;

public class ExpressionMethodBinding extends MethodBinding implements StateHolder {
	
	private MethodExpression exp;
	private ELContext elContext;
	private boolean isTransient;
	
	public ExpressionMethodBinding() {
		
	}

	public ExpressionMethodBinding(MethodExpression exp, ELContext elContext) {
		this.exp = exp;
		this.elContext = elContext;
	}

	@Override
	public Class<?> getType(FacesContext facesContext) throws MethodNotFoundException {
		return exp.getMethodInfo(elContext).getReturnType();
	}

	@Override
	public Object invoke(FacesContext facesContext, Object[] params) throws EvaluationException, MethodNotFoundException {
		return exp.invoke(elContext, params);
	}

	@Override
	public boolean isTransient() {
		return isTransient;
	}

	@Override
	public void restoreState(FacesContext facesContext, Object state) {
		Object[] stateArray = (Object[])state;
		this.exp = (MethodExpression)stateArray[0];
		this.elContext = new FacesELContext(EL3BindingFactory.getExpressionFactory());
	}

	@Override
	public Object saveState(FacesContext facesContext) {
		return new Object[] {
			exp
		};
	}

	@Override
	public void setTransient(boolean isTransient) {
		this.isTransient = isTransient;
	}

}
