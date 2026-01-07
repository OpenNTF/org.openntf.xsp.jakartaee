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
package org.openntf.xsp.jakarta.nosql.communication.driver.impl;

import java.time.Duration;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.jnosql.communication.semistructured.CommunicationEntity;
import org.openntf.xsp.jakarta.nosql.communication.driver.DominoDocumentManager;

/**
 * Contains common behavior used by distinct {@link DominoDocumentManager}
 * implementations.
 *
 * @author Jesse Gallagher
 * @since 2.8.0
 */
public abstract class AbstractDominoDocumentCollectionManager implements DominoDocumentManager {
	protected static final boolean transactionsAvailable;
	static {
		boolean found;
		try {
			Class.forName("jakarta.transaction.Transaction"); //$NON-NLS-1$
			found = true;
		} catch(Exception e) {
			found = false;
		}
		transactionsAvailable = found;
	}

	@Override
	public CommunicationEntity insert(final CommunicationEntity entity) {
		return insert(entity, false);
	}

	@Override
	public CommunicationEntity insert(final CommunicationEntity entity, final Duration ttl) {
		// TODO consider supporting ttl
		return insert(entity);
	}

	@Override
	public Iterable<CommunicationEntity> insert(final Iterable<CommunicationEntity> entities, final Duration ttl) {
		// TODO consider supporting ttl
		return insert(entities);
	}

	@Override
	public CommunicationEntity update(final CommunicationEntity entity) {
		return update(entity, false);
	}

	@Override
	public Iterable<CommunicationEntity> update(final Iterable<CommunicationEntity> entities) {
		if(entities == null) {
			return Collections.emptySet();
		} else {
			return StreamSupport.stream(entities.spliterator(), false)
				.map(this::update)
				.collect(Collectors.toList());
		}
	}
}
