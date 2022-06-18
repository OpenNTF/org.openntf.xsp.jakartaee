package org.openntf.xsp.nosql.communication.driver.lsxbe.util;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;

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
public class DocumentCollectionIterator implements Iterator<Document> {
	private final DocumentCollection docs;
	private final int size;
	private int fetched = 0;
	private Document prev;
	
	public DocumentCollectionIterator(DocumentCollection docs) {
		this.docs = docs;
		try {
			this.size = docs.getCount();
		} catch (NotesException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean hasNext() {
		return fetched < size;
	}

	@Override
	public Document next() {
		try {
			Document next;
			if(prev == null) {
				next = docs.getFirstDocument();
			} else {
				next = docs.getNextDocument(prev);
				prev.recycle();
			}
			prev = next;
			fetched++;
			
			if(!hasNext()) {
				docs.recycle();
			}
			
			return next;
		} catch(NotesException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Spliterator<Document> spliterator() {
		return Spliterators.spliterator(this, size, Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.SIZED);
	}
	
}