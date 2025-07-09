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
package org.openntf.xsp.jakarta.transaction.interceptor;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.Transactional;

/**
 * Basic CDI implementation of the {@link Transactional @Transactional} annotation.
 *
 * @author Jesse Gallagher
 * @since 2.7.0
 */
@Interceptor
@Transactional(Transactional.TxType.REQUIRES_NEW)
@Priority(Interceptor.Priority.PLATFORM_BEFORE+200)
public class TransactionalInterceptorRequiresNew extends AbstractTransactionalInterceptor {

	@AroundInvoke
	public Object wrapMethod(final InvocationContext ctx) throws Exception {
		TransactionManager man = CDI.current().select(TransactionManager.class).get();
		if(man.getTransaction() == null) {
			// Then we run the transaction
			man.begin();
			try {
				return super.doWrapMethod(ctx);
			} finally {
				man.commit();
			}
		} else {
			throw new UnsupportedOperationException("Domino Transactions implementation does not support suspending existing transactions");
		}
	}
}
