package org.openntf.xsp.nosql.communication.driver.lsxbe.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lotus.domino.NotesException;
import lotus.domino.ViewEntry;
import lotus.domino.ViewNavigator;

public class ViewNavigatorIterator implements Iterator<ViewEntry> {
	private final ViewNavigator nav;
	private ViewEntry prev;
	private ViewEntry onDeck;
	private boolean done;
	
	public ViewNavigatorIterator(ViewNavigator nav) throws NotesException {
		this.nav = nav;
	}

	@Override
	public boolean hasNext() {
		if(done) {
			return false;
		}
		if(onDeck == null) {
			try {
				onDeck = fetchNext();
			} catch (NotesException e) {
				throw new RuntimeException(e);
			}
		}
		return !done;
	}

	@Override
	public ViewEntry next() {
		if(done) {
			throw new NoSuchElementException();
		}
		
		try {
			if(onDeck == null) {
				onDeck = fetchNext();
			}
			if(done) {
				throw new NoSuchElementException();
			}
			if(prev != null) {
				prev.recycle();
			}
			prev = onDeck;
			onDeck = null;
			return prev;
		} catch(NotesException e) {
			throw new RuntimeException(e);
		}
	}

	public Stream<ViewEntry> stream() {
		Spliterator<ViewEntry> iter = Spliterators.spliteratorUnknownSize(this, Spliterator.DISTINCT | Spliterator.ORDERED);
		return StreamSupport.stream(iter, false);
	}
	
	private ViewEntry fetchNext() throws NotesException {
		ViewEntry next;
		if(prev == null) {
			next = nav.getFirst();
		} else {
			next = nav.getNext(prev);
		}
		if(next == null) {
			this.done = true;
			nav.recycle();
		}
		return next;
	}
}
