/**
 * Copyright © 2018-2020 Jesse Gallagher
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

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

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
	private String prefix;
	
	public ExpressionValueBinding() {
		
	}
	
	public ExpressionValueBinding(ValueExpression exp, ELContext elContext, String prefix) {
		this.exp = exp;
		this.elContext = elContext;
		this.prefix = prefix;
	}

	@Override
	public Class<?> getType(FacesContext facesContext) throws EvaluationException, PropertyNotFoundException {
		try {
			return AccessController.doPrivileged((PrivilegedExceptionAction<Class<?>>)() -> exp.getType(elContext));
		} catch (PrivilegedActionException e) {
			Throwable t = e.getCause();
			if(t instanceof EvaluationException) {
				throw (EvaluationException)t;
			} else if(t instanceof PropertyNotFoundException) {
				throw (PropertyNotFoundException)t;
			} else if(t instanceof Error) {
				throw (Error)t;
			} else if(t instanceof RuntimeException) {
				throw (RuntimeException)t;
			} else if(t != null) {
				throw new RuntimeException(t);
			} else {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public Object getValue(FacesContext facesContext) throws EvaluationException, PropertyNotFoundException {
		try {
			return AccessController.doPrivileged((PrivilegedExceptionAction<Object>)() -> exp.getValue(elContext));
		} catch (Throwable e) {
			Throwable t = e.getCause();
			if(t instanceof PropertyNotFoundException) {
				throw new PropertyNotFoundException("Encountered exception processing expression " + exp, t);
			} else if(t != null) {
				throw new EvaluationException("Encountered exception processing expression " + exp, t);
			} else {
				throw new EvaluationException("Encountered exception processing expression " + exp, e);
			}
		}
	}

	@Override
	public boolean isReadOnly(FacesContext facesContext) throws EvaluationException, PropertyNotFoundException {
		try {
			return AccessController.doPrivileged((PrivilegedExceptionAction<Boolean>)() -> exp.isReadOnly(elContext));
		} catch (PrivilegedActionException e) {
			Throwable t = e.getCause();
			if(t instanceof EvaluationException) {
				throw (EvaluationException)t;
			} else if(t instanceof PropertyNotFoundException) {
				throw (PropertyNotFoundException)t;
			} else if(t instanceof Error) {
				throw (Error)t;
			} else if(t instanceof RuntimeException) {
				throw (RuntimeException)t;
			} else if(t != null) {
				throw new RuntimeException(t);
			} else {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public void setValue(FacesContext facesContext, Object value) throws EvaluationException, PropertyNotFoundException {
		try {
			AccessController.doPrivileged((PrivilegedExceptionAction<Void>)() -> {
				exp.setValue(elContext, value);
				return null;
			});
		} catch (PrivilegedActionException e) {
			Throwable t = e.getCause();
			if(t instanceof EvaluationException) {
				throw (EvaluationException)t;
			} else if(t instanceof PropertyNotFoundException) {
				throw (PropertyNotFoundException)t;
			} else if(t instanceof Error) {
				throw (Error)t;
			} else if(t instanceof RuntimeException) {
				throw (RuntimeException)t;
			} else if(t != null) {
				throw new RuntimeException(t);
			} else {
				throw new RuntimeException(e);
			}
		}
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
		this.prefix = (String)stateArray[1];
	}

	@Override
	public Object saveState(FacesContext facesContext) {
		return new Object[] {
			this.exp,
			this.prefix
		};
	}

	@Override
	public void setTransient(boolean isTransient) {
		this.isTransient = isTransient;
	}
	
	@Override
	public String getExpressionString() {
		String expString = this.exp.getExpressionString();
		if(ValueBindingUtil.isValueBindingExpression(expString)) {
			// Then return it as-is
			return expString;
		} else {
			// Then wrap it in binding brackets
			return ValueBindingUtil.getExpressionString(prefix, this.exp.getExpressionString(), ValueBindingUtil.RUNTIME_EXPRESSION);
		}
	}
	
}