package org.openntf.xsp.jakarta.transaction.cdi;

import org.openntf.xsp.jakarta.transaction.interceptor.TransactionalInterceptorMandatory;
import org.openntf.xsp.jakarta.transaction.interceptor.TransactionalInterceptorNever;
import org.openntf.xsp.jakarta.transaction.interceptor.TransactionalInterceptorNotSupported;
import org.openntf.xsp.jakarta.transaction.interceptor.TransactionalInterceptorRequired;
import org.openntf.xsp.jakarta.transaction.interceptor.TransactionalInterceptorRequiresNew;
import org.openntf.xsp.jakarta.transaction.interceptor.TransactionalInterceptorSupports;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;

/**
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public class TransactionExtension implements Extension {
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addInterceptorBindings(@Observes BeforeBeanDiscovery bbd, BeanManager manager) {
		String extensionName = TransactionExtension.class.getName();

		for (Class clazz : new Class[] {
			TransactionalInterceptorRequired.class,
			TransactionalInterceptorRequiresNew.class,
			TransactionalInterceptorMandatory.class,
			TransactionalInterceptorNotSupported.class,
			TransactionalInterceptorSupports.class,
			TransactionalInterceptorNever.class
		}) {
			bbd.addAnnotatedType(manager.createAnnotatedType(clazz), extensionName + "_" + clazz.getName()); //$NON-NLS-1$
		}
	}

}
