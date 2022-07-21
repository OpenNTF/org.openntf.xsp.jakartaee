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
package org.openntf.xsp.jakarta.transaction.servlet;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.openntf.xsp.jakarta.transaction.DominoUserTransaction;

/**
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public interface AbstractTransactionJndiConfigurator {
	
	public static final String JNDI_USERTRANSACTION = "java:comp/UserTransaction"; //$NON-NLS-1$

	public static final String ATTR_EXECUTORSERVICE = AbstractTransactionJndiConfigurator.class.getName() + "_exec"; //$NON-NLS-1$
	

	default void pushTransaction() {
		try {
			InitialContext jndi = new InitialContext();
			jndi.rebind(JNDI_USERTRANSACTION, DominoUserTransaction.SHARED_INSTANCE);
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}
	
	default void popTransaction() {
		try {
			InitialContext jndi = new InitialContext();
			
			jndi.unbind(JNDI_USERTRANSACTION);
		} catch(NameNotFoundException e) {
			// Ignore - that's fine
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}

}
