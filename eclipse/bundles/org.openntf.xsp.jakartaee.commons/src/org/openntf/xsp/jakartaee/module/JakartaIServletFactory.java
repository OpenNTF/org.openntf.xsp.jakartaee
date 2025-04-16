package org.openntf.xsp.jakartaee.module;

import com.ibm.designer.runtime.domino.adapter.IServletFactory;

/**
 * This subinterface of {@link IServletFactory} designates an implication
 * that is "Jakarta-aware": that is, it expects to handle modules that
 * are not always {@code NSFComponentModule}.
 * 
 * @since 3.4.0
 */
public interface JakartaIServletFactory extends IServletFactory {

}
