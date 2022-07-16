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
package rest;

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

@Path("transaction")
public class TransactionExample {
	
	@Inject
	private UserTransaction transaction;
	
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
}
