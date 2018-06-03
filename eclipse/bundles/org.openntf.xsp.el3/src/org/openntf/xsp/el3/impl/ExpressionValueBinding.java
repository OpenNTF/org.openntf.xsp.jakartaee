package org.openntf.xsp.el3.impl;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.component.StateHolder;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.ValueBinding;

import org.openntf.xsp.el3.EL3BindingFactory;

public class ExpressionValueBinding extends ValueBinding implements StateHolder {
	
	private ValueExpression exp;
	private ELContext elContext;
	private boolean isTransient;
	
	public ExpressionValueBinding() {
		
	}
	
	public ExpressionValueBinding(ValueExpression exp, ELContext elContext) {
		this.exp = exp;
		this.elContext = elContext;
	}

	@Override
	public Class<?> getType(FacesContext facesContext) throws EvaluationException, PropertyNotFoundException {
		return exp.getType(elContext);
	}

	@Override
	public Object getValue(FacesContext facesContext) throws EvaluationException, PropertyNotFoundException {
		return exp.getValue(elContext);
	}

	@Override
	public boolean isReadOnly(FacesContext facesContext) throws EvaluationException, PropertyNotFoundException {
		return exp.isReadOnly(elContext);
	}

	@Override
	public void setValue(FacesContext facesContext, Object value) throws EvaluationException, PropertyNotFoundException {
		exp.setValue(elContext, value);
	}

	@Override
	public boolean isTransient() {
		return isTransient;
	}

	@Override
	public void restoreState(FacesContext facesContext, Object state) {
		Object[] stateArray = (Object[])state;
		this.exp = (ValueExpression)stateArray[0];
		this.elContext = new FacesELContext(EL3BindingFactory.getExpressionFactory());
	}

	@Override
	public Object saveState(FacesContext facesContext) {
		return new Object[] {
			this.exp
		};
	}

	@Override
	public void setTransient(boolean isTransient) {
		this.isTransient = isTransient;
	}
	
}