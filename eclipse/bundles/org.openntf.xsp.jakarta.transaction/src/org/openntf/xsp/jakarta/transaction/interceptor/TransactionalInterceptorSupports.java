package org.openntf.xsp.jakarta.transaction.interceptor;

import jakarta.annotation.Priority;
import jakarta.interceptor.Interceptor;
import jakarta.transaction.Transactional;

/**
 * Basic CDI implementation of the {@link Transactional @Transactional} annotation.
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
@Interceptor
@Transactional(Transactional.TxType.SUPPORTS)
@Priority(Interceptor.Priority.LIBRARY_BEFORE)
public class TransactionalInterceptorSupports extends AbstractTransactionalInterceptor {

}
