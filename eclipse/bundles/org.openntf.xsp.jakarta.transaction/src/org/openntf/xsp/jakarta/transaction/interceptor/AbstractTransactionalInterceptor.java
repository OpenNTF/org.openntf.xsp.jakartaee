/**
 * Copyright © 2018-2022 Contributors to the XPages Jakarta EE Support Project
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
