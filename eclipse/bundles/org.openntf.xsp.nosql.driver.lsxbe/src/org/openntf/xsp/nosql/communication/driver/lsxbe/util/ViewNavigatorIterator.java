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
	private final boolean docsOnly;
	private ViewEntry prev;
	private ViewEntry onDeck;
	private boolean done;
	
	public ViewNavigatorIterator(ViewNavigator nav, boolean docsOnly) throws NotesException {
		this.nav = nav;
		this.docsOnly = docsOnly;
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
			if(docsOnly) {
				next = nav.getFirstDocument();
			} else {
				next = nav.getFirst();
			}
		} else {
			if(docsOnly) {
				next = nav.getNextDocument();
			} else {
				next = nav.getNext(prev);
			}
		}
		if(next == null) {
			this.done = true;
			nav.recycle();
		}
		return next;
	}
}
