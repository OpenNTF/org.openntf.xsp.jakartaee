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
package org.openntf.xsp.el3;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.application.Application;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;

import org.openntf.xsp.el3.impl.ExpressionMethodBinding;
import org.openntf.xsp.el3.impl.ExpressionValueBinding;
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
	
	private final String prefix;
	public EL3BindingFactory(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public MethodBinding createMethodBinding(Application application, String expression, @SuppressWarnings("rawtypes") Class[] args) {
		ELContext context = new FacesELContext(fac);
		
		String cleanExp;
		int prefixIndex = expression.indexOf(prefix + ':');
		if(prefixIndex > -1) {
			cleanExp = expression.substring(0, prefixIndex) + expression.substring(prefixIndex+prefix.length()+1);
		} else {
			cleanExp = expression;
		}
		MethodExpression exp = fac.createMethodExpression(context, cleanExp, Object.class, args == null ? new Class[0] : args);
		
		return new ExpressionMethodBinding(exp, context, prefix);
	}

	@Override
	public ValueBinding createValueBinding(Application application, String expression) {
		ELContext context = new FacesELContext(fac);
		
		String cleanExp;
		int prefixIndex = expression.indexOf(prefix + ':');
		if(prefixIndex > -1) {
			cleanExp = expression.substring(0, prefixIndex) + expression.substring(prefixIndex+prefix.length()+1);
		} else {
			cleanExp = expression;
		}
		
		ValueExpression exp = fac.createValueExpression(context, cleanExp, Object.class);
		
		return new ExpressionValueBinding(exp, context, prefix);
	}

	@Override
	public String getPrefix() {
		return prefix;
	}
}
