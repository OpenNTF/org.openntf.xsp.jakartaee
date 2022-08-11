package org.openntf.xsp.nosql.communication.driver.impl;

import java.time.Duration;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.eclipse.jnosql.mapping.reflection.ClassInformationNotFoundException;
import org.eclipse.jnosql.mapping.reflection.ClassMapping;
import org.eclipse.jnosql.mapping.reflection.ClassMappings;
import org.openntf.xsp.nosql.communication.driver.DominoDocumentCollectionManager;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.nosql.document.DocumentEntity;

/**
 * Contains common behavior used by distinct {@link DominoDocumentCollectionManager}
 * implementations.
 * 
 * @author Jesse Gallagher
 * @since 2.8.0
 */
public abstract class AbstractDominoDocumentCollectionManager implements DominoDocumentCollectionManager {
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
	public DocumentEntity insert(DocumentEntity entity) {
		return insert(entity, false);
	}

	@Override
	public DocumentEntity insert(DocumentEntity entity, Duration ttl) {
		// TODO consider supporting ttl
		return insert(entity);
	}

	@Override
	public Iterable<DocumentEntity> insert(Iterable<DocumentEntity> entities, Duration ttl) {
		// TODO consider supporting ttl
		return insert(entities);
	}
	
	@Override
	public DocumentEntity update(DocumentEntity entity) {
		return update(entity, false);
	}

	@Override
	public Iterable<DocumentEntity> update(Iterable<DocumentEntity> entities) {
		if(entities == null) {
			return Collections.emptySet();
		} else {
			return StreamSupport.stream(entities.spliterator(), false)
				.map(this::update)
				.collect(Collectors.toList());
		}
	}

	protected ClassMapping getClassMapping(String entityName) {
		ClassMappings mappings = CDI.current().select(ClassMappings.class).get();
		try {
			return mappings.findByName(entityName);
		} catch(ClassInformationNotFoundException e) {
			// Shouldn't happen, but we should account for it
			return null;
		}
	}

}
