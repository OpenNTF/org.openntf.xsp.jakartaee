/*
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
package org.openntf.xsp.jakarta.nosql.communication.driver.lsxbe.util;

import static org.openntf.xsp.jakarta.nosql.communication.driver.lsxbe.util.DominoNoSQLUtil.recycle;

import java.util.Iterator;

import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.NotesException;

/**
 * This {@link Iterator} implementation will iterate over a {@link DocumentCollection}
 * instance.
 *
 * <p>Each {@link Document} instance is recycled after the call to {@link #next} for the
 * next one. After emitting the final document in the collection, the
 * {@link DocumentCollection} will be recycled.
 *
 * @author Jesse Gallagher
 * @since 2.6.0
 */
public class DocumentCollectionIterator extends AbstractCollectionIterator<Document> implements AutoCloseable {
	private final DocumentCollection docs;
	private Document prev;

	public DocumentCollectionIterator(final DocumentCollection docs) throws NotesException {
		super(docs.getCount());
		this.docs = docs;
	}

	@Override
	public Document next() {
		try {
			Document next;
			if(prev == null) {
				next = docs.getFirstDocument();
			} else {
				next = docs.getNextDocument(prev);
				recycle(prev);
			}
			prev = next;
			fetched++;

			if(!hasNext()) {
				recycle(docs);
			}

			return next;
		} catch(NotesException e) {
			throw new UncheckedNotesException(e);
		}
	}

	@Override
	public void close() {
		recycle(prev, docs);
	}

}