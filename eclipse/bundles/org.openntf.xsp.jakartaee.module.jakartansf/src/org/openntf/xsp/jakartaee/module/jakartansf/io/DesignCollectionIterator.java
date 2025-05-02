package org.openntf.xsp.jakartaee.module.jakartansf.io;

import java.util.Iterator;

import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesCollection;
import com.ibm.designer.domino.napi.NotesCollectionEntry;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.designer.domino.napi.util.NotesIterator;

public class DesignCollectionIterator implements Iterator<NotesCollectionEntry>, AutoCloseable {
	private NotesCollection collection;
	private NotesIterator iter;
	
	public DesignCollectionIterator(NotesDatabase db) {
		try {
			this.collection = db.openCollection(-0xFFe0, 0);
			this.iter = this.collection.readEntries(32775, 0, 32);
		} catch (NotesAPIException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean hasNext() {
		return iter.hasNext();
	}

	@Override
	public NotesCollectionEntry next() {
		return (NotesCollectionEntry)iter.next();
	}
	
	@Override
	public void close() {
		try {
			iter.recycle();
		} catch (NotesAPIException e) {
			// Ignore
		}
		try {
			collection.recycle();
		} catch (NotesAPIException e) {
			// Ignore
		}
	}

}
