/**
 * Copyright © 2019 Jesse Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.xsp.el3.impl;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.component.StateHolder;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.ValueBinding;

import org.openntf.xsp.el3.EL3BindingFactory;

import com.ibm.xsp.util.ValueBindingUtil;

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
	
	@Override
	public String getExpressionString() {
		return ValueBindingUtil.getExpressionString(EL3BindingFactory.PREFIX, this.exp.getExpressionString(), 1);
	}
	
}