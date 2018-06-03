package org.openntf.xsp.el3;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.component.StateHolder;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.MethodBinding;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.ValueBinding;

import org.openntf.xsp.el3.impl.FacesELContext;

import com.ibm.xsp.binding.BindingFactory;

/**
 * This class provides an Expression Language 3.0 interpreter instead of the stock
 * XPages EL interpreter.
 * 
 * <p>Note: to get XPages to compile runtime bindings with method calls, prefix the
 * expression with {@code el:}, e.g. "<code>#{el:foo.bar()}</code>".
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class EL3BindingFactory implements BindingFactory {
	
	public static final String PREFIX = "el"; //$NON-NLS-1$
	
	private static final ExpressionFactory fac = ExpressionFactory.newInstance();
	
	public static ExpressionFactory getExpressionFactory() {
		return fac;
	}

	@Override
	public MethodBinding createMethodBinding(Application application, String expression, @SuppressWarnings("rawtypes") Class[] args) {
		
		return null;
	}

	@Override
	public ValueBinding createValueBinding(Application application, String expression) {
		ELContext context = new FacesELContext(fac);
		
		String cleanExp;
		int prefixIndex = expression.indexOf(PREFIX + ':');
		if(prefixIndex > -1) {
			cleanExp = expression.substring(0, prefixIndex) + expression.substring(prefixIndex+PREFIX.length()+1);
		} else {
			cleanExp = expression;
		}
		
		ValueExpression exp = fac.createValueExpression(context, cleanExp, Object.class);
		
		return new ExpressionValueBinding(exp, context);
	}

	@Override
	public String getPrefix() {
		return PREFIX;
	}
	
	private static class ExpressionValueBinding extends ValueBinding implements StateHolder {
		
		private ValueExpression exp;
		private ELContext elContext;
		private boolean isTransient;
		
		@SuppressWarnings("unused")
		private ExpressionValueBinding() {
			
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
			this.elContext = new FacesELContext(getExpressionFactory());
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
}
