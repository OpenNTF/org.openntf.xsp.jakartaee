/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakartaee.module.jakartansf.io;

import java.text.MessageFormat;
import java.util.Iterator;

import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesCollection;
import com.ibm.designer.domino.napi.NotesCollectionEntry;
import com.ibm.designer.domino.napi.NotesConstants;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.designer.domino.napi.util.NotesIterator;

public class DesignCollectionIterator implements Iterator<DesignCollectionIterator.DesignEntry>, AutoCloseable {
	public record DesignEntry(String flags, String title, String classIndexItem, int noteId, Runnable recycler) implements AutoCloseable {
		public DesignEntry(NotesCollectionEntry entry) {
			this(
				getItemValueAsString(entry, NotesConstants.DESIGN_FLAGS),
				getItemValueAsString(entry, NotesConstants.FIELD_TITLE),
				getItemValueAsString(entry, "$ClassIndexItem"), //$NON-NLS-1$
				getNoteID(entry),
				() -> {
					try {
						entry.recycle();
					} catch (NotesAPIException e) {
						// Ignore
					}
				}
			);
		}
		
		@Override
		public void close() {
			recycler.run();
		}
	}
	
	private NotesCollection collection;
	private NotesIterator iter;

	public DesignCollectionIterator(NotesDatabase db) {
		try {
			int designNoteId = -0xFFe0;
			this.collection = db.openCollection(designNoteId, 0);
			if (!this.collection.isValidHandle()) {
				throw new RuntimeException(
						MessageFormat.format("Unable to open design collection in {0}", db.getDatabasePath()));
			}
			this.iter = this.collection.readEntries(32775, 0, 32);
		} catch (NotesAPIException e) {
			if(e.getNativeErrorCode() == 0x242) {
				// Special database object cannot be located: the design collection isn't initialized yet
				
			} else {
				throw new RuntimeException(
					MessageFormat.format("Encountered exception 0x{0} reading design collection: {1}",
							Integer.toHexString(e.getNativeErrorCode()), e.getLocalizedMessage()),
					e);
			}
		}
	}

	@Override
	public boolean hasNext() {
		return iter.hasNext();
	}

	@Override
	public DesignEntry next() {
		return new DesignEntry((NotesCollectionEntry) iter.next());
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
	
	private static String getItemValueAsString(NotesCollectionEntry entry, String itemName) {
		try {
			return entry.getItemValueAsString(itemName);
		} catch (NotesAPIException e) {
			throw new RuntimeException(e);
		}
	}
	private static int getNoteID(NotesCollectionEntry entry) {
		try {
			return entry.getNoteID();
		} catch (NotesAPIException e) {
			throw new RuntimeException(e);
		}
	}
}
