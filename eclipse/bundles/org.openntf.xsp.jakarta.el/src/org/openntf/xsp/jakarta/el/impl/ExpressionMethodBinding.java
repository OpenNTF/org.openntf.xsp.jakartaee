/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakarta.el.impl;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.faces.component.StateHolder;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.MethodBinding;
import javax.faces.el.MethodNotFoundException;

import com.ibm.xsp.util.ValueBindingUtil;

import org.openntf.xsp.jakarta.el.ELBindingFactory;

import jakarta.el.ELContext;
import jakarta.el.MethodExpression;

public class ExpressionMethodBinding extends MethodBinding implements StateHolder {

	private MethodExpression exp;
	private ELContext elContext;
	private boolean isTransient;
	private String prefix;

	public ExpressionMethodBinding() {

	}

	public ExpressionMethodBinding(final MethodExpression exp, final ELContext elContext, final String prefix) {
		this.exp = exp;
		this.elContext = elContext;
		this.prefix = prefix;
	}

	@Override
	public Class<?> getType(final FacesContext facesContext) throws MethodNotFoundException {
		try {
			return AccessController.doPrivileged((PrivilegedExceptionAction<Class<?>>)() -> exp.getMethodInfo(elContext).getReturnType());
		} catch (PrivilegedActionException e) {
			Throwable t = e.getCause();
			if(t instanceof MethodNotFoundException mnfe) {
				throw mnfe;
			} else if(t instanceof Error err) {
				throw err;
			} else if(t instanceof RuntimeException re) {
				throw re;
			} else if(t != null) {
				throw new RuntimeException(t);
			} else {
				throw new RuntimeException(e);
			}
		}
	}

	@Override
	public Object invoke(final FacesContext facesContext, final Object[] params) throws EvaluationException, MethodNotFoundException {
		try {
			return AccessController.doPrivileged((PrivilegedExceptionAction<Object>)() -> exp.invoke(elContext, params));
		} catch (PrivilegedActionException e) {
			Throwable t = e.getCause();
			if(t instanceof MethodNotFoundException) {
				throw (MethodNotFoundException)t;
			} else if(t instanceof EvaluationException) {
				throw (EvaluationException)t;
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
	public void restoreState(final FacesContext facesContext, final Object state) {
		Object[] stateArray = (Object[])state;
		this.exp = (MethodExpression)stateArray[0];
		this.elContext = new FacesELContext(ELBindingFactory.getExpressionFactory());
		this.prefix = (String)stateArray[1];
	}

	@Override
	public Object saveState(final FacesContext facesContext) {
		return new Object[] {
			this.exp,
			this.prefix
		};
	}

	@Override
	public void setTransient(final boolean isTransient) {
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
