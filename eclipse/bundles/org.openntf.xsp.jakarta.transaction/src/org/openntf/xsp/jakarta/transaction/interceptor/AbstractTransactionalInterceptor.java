package org.openntf.xsp.jakarta.transaction.interceptor;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import jakarta.transaction.Transactional;
import jakarta.transaction.UserTransaction;

/**
 * Basic CDI implementation of the {@link Transactional @Transactional} annotation.
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public abstract class AbstractTransactionalInterceptor {

	@AroundInvoke
	public Object wrapMethod(InvocationContext ctx) throws Exception {
		UserTransaction transaction = CDI.current().select(UserTransaction.class).get();
		try {
			transaction.begin();
			Object result = ctx.proceed();
			transaction.commit();
			return result;
		} catch(Throwable e) {
			// TODO check dontRollbackOn
			transaction.rollback();
			throw e;
		}
	}

}
