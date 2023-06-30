package org.openntf.xsp.el.ext;

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;

/**
 * This service interface allows for code to handle conversion of
 * types when reading and writing in between objects and an
 * XPages control.
 * 
 * @since 2.13.0
 */
public interface ELValueConverter {
	/**
	 * Called before setting the value on the expression binding.
	 * 
	 * @param elContext the evaluating {@link ELContext}
	 * @param exp the original {@link ValueExpression} to be called
	 * @param value the incoming value
	 * @return the filtered value
	 */
	default Object preSetValue(ELContext elContext, ValueExpression exp, Object value) {
		return value;
	}
	
	/**
	 * Called after retrieving the value from the expression binding.
	 * 
	 * @param elContext the evaluating {@link ELContext}
	 * @param exp the original {@link ValueExpression} that was be called
	 * @param value the retrieved value
	 * @return the filtered value
	 */
	default Object postGetValue(ELContext elContext, ValueExpression exp, Object value) {
		return value;
	}
}
