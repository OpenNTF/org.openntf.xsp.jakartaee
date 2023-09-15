/**
 * Copyright (c) 2018-2023 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.el.impl;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.text.MessageFormat;

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;
import javax.faces.component.StateHolder;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.ValueBinding;

import org.openntf.xsp.el.ELBindingFactory;
import org.openntf.xsp.el.ext.ELValueConverter;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

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
			return AccessController.doPrivileged((PrivilegedExceptionAction<Object>)() -> {
				Object v = exp.getValue(elContext);
				
				for(ELValueConverter conv : LibraryUtil.findExtensionsSorted(ELValueConverter.class, false)) {
					v = conv.postGetValue(elContext, exp, v);
				}
				
				return v;
			});
		} catch (Throwable e) {
			Throwable t = e.getCause();
			if(t instanceof PropertyNotFoundException) {
				throw new PropertyNotFoundException(MessageFormat.format(Messages.getString("ExpressionValueBinding.exceptionProcessingExpression"), exp), t); //$NON-NLS-1$
			} else if(t != null) {
				throw new EvaluationException(MessageFormat.format(Messages.getString("ExpressionValueBinding.exceptionProcessingExpression"), exp), t); //$NON-NLS-1$
			} else {
				throw new EvaluationException(MessageFormat.format(Messages.getString("ExpressionValueBinding.exceptionProcessingExpression"), exp), e); //$NON-NLS-1$
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
				Object v = value;
				for(ELValueConverter conv : LibraryUtil.findExtensionsSorted(ELValueConverter.class, false)) {
					v = conv.preSetValue(elContext, exp, v);
				}
				
				exp.setValue(elContext, v);
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
		this.elContext = new FacesELContext(ELBindingFactory.getExpressionFactory());
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