/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
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
package rest;

import java.io.PrintWriter;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import bean.TransactionScopeBean;
import jakarta.inject.Inject;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transactional;
import jakarta.transaction.UserTransaction;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.StreamingOutput;

@Path("transaction")
public class TransactionExample {
	
	@Inject
	private UserTransaction transaction;
	
	@Inject
	private TransactionScopeBean transactionScopeBean;
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String get() throws NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
		transaction.begin();
		transaction.commit();
		return "committed.";
	}
	
	@Path("rollbackOnly")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getRollback() throws IllegalStateException, SecurityException, SystemException, NotSupportedException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
		transaction.begin();
		transaction.setRollbackOnly();
		transaction.commit();
		return "should have thrown a rolled-back exception";
	}
	
	@Path("annotated")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Transactional
	public String getAnnotated() {
		return "committed via transactional REST method";
	}
	
	@Path("jndi")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getJndi() throws NamingException {
		return "I found: " + (UserTransaction)InitialContext.doLookup("java:comp/UserTransaction");
	}
	
	@Path("scope")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public StreamingOutput getScope() {
		return out -> {
			try(PrintWriter w = new PrintWriter(out)) {
			
				try {
					transaction.begin();
					w.println("current val: " + transactionScopeBean.getCreated());
					transaction.commit();
					
					Thread.sleep(1);
					
					try {
						transactionScopeBean.getCreated();
					} catch(Throwable t) {
						w.println("got expected exception: " + t.getClass().getName() + ": " + t.getMessage());
					}
					
					transaction.begin();
					w.println("later val: " + transactionScopeBean.getCreated());
					transaction.commit();
				} catch(Exception e) {
					e.printStackTrace(w);
				}
			}
		};
	}
}
