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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.commons.util.io.ByteStreamCache;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesCollectionEntry;
import com.ibm.designer.domino.napi.NotesConstants;
import com.ibm.designer.domino.napi.NotesNote;
import com.ibm.designer.domino.napi.design.FileAccess;
import com.ibm.designer.domino.napi.util.NotesUtils;

import org.openntf.xsp.jakartaee.module.jakartansf.NSFJakartaModule;

public class NSFJakartaFileSystem {
	private static final Logger log = Logger.getLogger(NSFJakartaFileSystem.class.getPackageName());
	public static final String URLSCHEME = "jakartansf"; //$NON-NLS-1$
	
	private final Map<String, Integer> fileMap = new HashMap<>();
	private final NSFJakartaModule module;
	
	public NSFJakartaFileSystem(NSFJakartaModule module) {
		this.module = module;
		
		try (DesignCollectionIterator nav = new DesignCollectionIterator(module.getNotesDatabase())) {
			while (nav.hasNext()) {
				NotesCollectionEntry entry = nav.next();

				String flags = entry.getItemValueAsString(NotesConstants.DESIGN_FLAGS);
				if(NotesUtils.CmemflagTestMultiple(flags, NotesConstants.DFLAGPAT_FILE_WEB)) {
					// In practice, we don't care about $ClassIndexItem
					String name = sanitizeTitle(entry.getItemValueAsString(NotesConstants.FIELD_TITLE));
					if(NotesUtils.CmemflagTestMultiple(flags, NotesConstants.DFLAGPAT_JAVAJAR)) {
						name = "WEB-INF/lib/" + name; //$NON-NLS-1$
					}
					int noteId = entry.getNoteID();
					if(log.isLoggable(Level.FINEST)) {
						log.finest(MessageFormat.format("Adding file element \"{0}\", note ID 0x{1}", name, Integer.toHexString(noteId)));
					}
					fileMap.put(name, noteId);
				} else if(NotesUtils.CmemflagTestMultiple(flags, NotesConstants.DFLAGPAT_SCRIPTLIB_JS)) {
					String name = sanitizeTitle(entry.getItemValueAsString(NotesConstants.FIELD_TITLE));
					int noteId = entry.getNoteID();
					if(log.isLoggable(Level.FINEST)) {
						log.finest(MessageFormat.format("Adding JavaScript library \"{0}\", note ID 0x{1}", name, Integer.toHexString(noteId)));
					}
					fileMap.put(name, noteId);
				} else if(NotesUtils.CmemflagTestMultiple(flags, NotesConstants.DFLAGPAT_IMAGE_RES_WEB)) {
					String name = sanitizeTitle(entry.getItemValueAsString(NotesConstants.FIELD_TITLE));
					int noteId = entry.getNoteID();
					if(log.isLoggable(Level.FINEST)) {
						log.finest(MessageFormat.format("Adding image resource \"{0}\", note ID 0x{1}", name, Integer.toHexString(noteId)));
					}
					fileMap.put(name, noteId);
				} else if(NotesUtils.CmemflagTestMultiple(flags, NotesConstants.DFLAGPAT_STYLE_SHEETS_WEB)) {
					String name = sanitizeTitle(entry.getItemValueAsString(NotesConstants.FIELD_TITLE));
					int noteId = entry.getNoteID();
					if(log.isLoggable(Level.FINEST)) {
						log.finest(MessageFormat.format("Adding stylesheet \"{0}\", note ID 0x{1}", name, Integer.toHexString(noteId)));
					}
					fileMap.put(name, noteId);
				}
				// TODO Re-add when figuring out how to read the value, or handle elsewhere
//				else if(entry.getNoteClass() == NotesConstants.NOTE_CLASS_ICON) {
//					int noteId = entry.getNoteID();
//					if(log.isLoggable(Level.FINEST)) {
//						log.finest(MessageFormat.format("Adding Icon note \"$Icon\", note ID 0x{0}", Integer.toHexString(noteId)));
//					}
//					fileMap.put("$Icon", noteId); //$NON-NLS-1$
//				}

				entry.recycle();
			}
			
		} catch (NotesAPIException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Optional<URL> getUrl(String res) {
		if(this.fileMap.containsKey(res)) {
			try {
				URI uri = new URI(URLSCHEME, null, "//" + res, null, null); //$NON-NLS-1$
				return Optional.of(URL.of(uri, new NSFJakartaURLStreamHandler(res)));
			} catch(URISyntaxException | MalformedURLException e) {
				throw new RuntimeException(MessageFormat.format("Encountered exception constructing URL for resource \"{0}\" in {1}", res, module.getMapping().nsfPath()), e);
			}
		} else {
			return Optional.empty();
		}
	}
	
	public Optional<InputStream> openStream(String res) {
		Integer noteId = this.fileMap.get(res);
		if(noteId != null) {
			try {
				NotesNote note = module.getNotesDatabase().openNote(noteId, 0);
				if(note != null) {
					// TODO special handling for icon note
					ByteStreamCache cache = new ByteStreamCache();
					FileAccess.readFileContent(note, cache.getOutputStream());
					return Optional.of(cache.getInputStream());
				} else {
					return null;
				}
			} catch(NotesAPIException e) {
				throw new RuntimeException(MessageFormat.format("Encountered exception opening stream for resource \"{0}\" in {1}", res, module.getMapping().nsfPath()), e);
			}
		} else {
			return Optional.empty();
		}
	}
	
	private static String sanitizeTitle(String title) {
		return title.replace('\\', '/');
	}
	
	private class NSFJakartaURLStreamHandler extends URLStreamHandler {
		private final String path;
		
		public NSFJakartaURLStreamHandler(String path) {
			this.path = path;
		}

		@Override
		protected URLConnection openConnection(URL u) throws IOException {
			return new NSFJakartaURLConnection(u, path);
		}
		
	}
	
	private class NSFJakartaURLConnection extends URLConnection {
		private final String res;

		protected NSFJakartaURLConnection(URL url, String res) {
			super(url);
			this.res = res;
		}

		@Override
		public void connect() throws IOException {
			// NOP
		}
		
		@Override
		public InputStream getInputStream() throws IOException {
			return openStream(res)
				.orElseThrow(() -> new IllegalStateException(MessageFormat.format("Unable to find resource \"{0}\" in {1}", res, module.getMapping().nsfPath())));
		}

	}
}
