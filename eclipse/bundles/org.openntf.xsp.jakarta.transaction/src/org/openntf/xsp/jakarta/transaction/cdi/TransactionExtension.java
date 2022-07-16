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
