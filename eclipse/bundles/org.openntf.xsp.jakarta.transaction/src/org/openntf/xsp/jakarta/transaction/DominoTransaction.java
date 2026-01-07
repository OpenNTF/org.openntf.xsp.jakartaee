/**
 * Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakarta.transaction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.UserTransaction;

/**
 * Domino implementation of {@link UserTransaction}, which uses an IBM Commons
 * extension point for {@link TransactionParticipant} to hook into the
 * transaction process.
 *
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public class DominoTransaction implements Transaction {
	private static final Logger log = Logger.getLogger(DominoTransaction.class.getName());

	private int status = Status.STATUS_NO_TRANSACTION;
	private boolean rollbackOnly = false;

	private final Collection<XAResource> resources;
	private final Collection<Synchronization> synchronizations;
	private final Xid id;

	public DominoTransaction(final Xid id) {
		this.resources = Collections.synchronizedList(new ArrayList<>());
		this.synchronizations = Collections.synchronizedList(new ArrayList<>());
		this.id = id;
	}

	public void begin() {
		for(XAResource res : new ArrayList<>(resources)) {
			try {
				res.start(id, XAResource.TMNOFLAGS);
			} catch (XAException e) {
				throw new RuntimeException(e);
			}
		}

		this.status = Status.STATUS_ACTIVE;
	}

	@Override
	public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
			SecurityException, IllegalStateException, SystemException {
		if(status == Status.STATUS_NO_TRANSACTION) {
			throw new IllegalStateException("Transaction was not begun");
		}
		if(rollbackOnly) {
			rollback();
			throw new RollbackException("Transaction was marked as rollback-only and rolled back");
		}

		status = Status.STATUS_COMMITTING;
		for(Synchronization sync : new ArrayList<>(this.synchronizations)) {
			sync.beforeCompletion();
		}
		try {
			for(XAResource res : new ArrayList<>(this.resources)) {
				try {
					res.commit(this.id, true);
				} catch (XAException e) {
					throw new RuntimeException(e);
				}
			}
			status = Status.STATUS_NO_TRANSACTION;
		} finally {
			for(Synchronization sync : new ArrayList<>(this.synchronizations)) {
				sync.afterCompletion(status);
			}
		}
	}

	@Override
	public void rollback() throws IllegalStateException, SecurityException, SystemException {
		if(status == Status.STATUS_NO_TRANSACTION) {
			throw new IllegalStateException("Transaction was not begun");
		}
		status = Status.STATUS_ROLLING_BACK;
		try {
			for(XAResource res : new ArrayList<>(this.resources)) {
				try {
					res.rollback(this.id);
				} catch (XAException e) {
					throw new RuntimeException(e);
				}
			}
			status = Status.STATUS_NO_TRANSACTION;
		} finally {
			for(Synchronization sync : new ArrayList<>(this.synchronizations)) {
				sync.afterCompletion(status);
			}
		}
	}

	@Override
	public void setRollbackOnly() throws IllegalStateException, SystemException {
		if(status == Status.STATUS_NO_TRANSACTION) {
			throw new IllegalStateException("Transaction was not begun");
		}
		this.rollbackOnly = true;
	}

	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public boolean delistResource(final XAResource xaRes, final int flag) throws IllegalStateException, SystemException {
		if(xaRes != null) {
			Iterator<XAResource> iter = this.resources.iterator();
			while(iter.hasNext()) {
				XAResource res = iter.next();
				try {
					if(xaRes.isSameRM(res)) {
						iter.remove();
						return true;
					}
				} catch (XAException e) {
					if(log.isLoggable(Level.SEVERE)) {
						log.log(Level.SEVERE, "Encountered exception matching existing resources", e);
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean enlistResource(final XAResource xaRes) throws RollbackException, IllegalStateException, SystemException {
		if(xaRes != null) {
			if(this.resources.stream().anyMatch(res -> {
				try {
					return xaRes.isSameRM(res);
				} catch (XAException e) {
					if(log.isLoggable(Level.SEVERE)) {
						log.log(Level.SEVERE, "Encountered exception matching existing resources", e);
					}
					return false;
				}
			})) {
				// Don't re-add the same database
				return false;
			}
			this.resources.add(xaRes);
			return true;
		}
		return false;
	}

	@Override
	public void registerSynchronization(final Synchronization sync)
			throws RollbackException, IllegalStateException, SystemException {
		this.synchronizations.add(sync);
	}

	public Collection<XAResource> getResources() {
		return Collections.unmodifiableCollection(resources);
	}

	public Xid getId() {
		return id;
	}

}
