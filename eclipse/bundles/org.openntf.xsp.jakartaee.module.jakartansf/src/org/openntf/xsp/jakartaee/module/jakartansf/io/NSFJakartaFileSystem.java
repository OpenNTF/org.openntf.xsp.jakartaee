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
package org.openntf.xsp.jakartaee.module.jakartansf.io;

import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesConstants;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.designer.domino.napi.NotesNote;
import com.ibm.designer.domino.napi.design.FileAccess;
import com.ibm.designer.domino.napi.util.NotesUtils;

import org.openntf.xsp.jakartaee.module.jakarta.ModuleFileSystem;
import org.openntf.xsp.jakartaee.module.jakartansf.NSFJakartaModule;
import org.openntf.xsp.jakartaee.module.jakartansf.io.DesignCollectionIterator.DesignEntry;
import org.openntf.xsp.jakartaee.util.ModuleUtil;

public class NSFJakartaFileSystem implements ModuleFileSystem {
	public record NSFMetadata(int noteId, FileType fileType, String flags, String flagsExt, boolean webVisible, String itemName, String mimeType) {
		public NSFMetadata(DesignEntry entry, FileType fileType, String mimeType) {
			this(entry.noteId(), fileType, entry.flags(), entry.flagsExt(), isWebVisible(entry, fileType), null, mimeType);
		}
		public NSFMetadata(DesignEntry entry, FileType fileType, String itemName, String mimeType) {
			this(entry.noteId(), fileType, entry.flags(), entry.flagsExt(), isWebVisible(entry, fileType), itemName, mimeType);
		}
	}
	public enum FileType {
		FILE, JAVASCRIPT, IMAGE, STYLESHEET, JAVA_CLASS
	}
	
	private static final Logger log = System.getLogger(NSFJakartaFileSystem.class.getPackageName());
	public static final String URLSCHEME = "jakartansf"; //$NON-NLS-1$
	
	private final Map<String, NSFMetadata> fileMap = new HashMap<>();
	private final NSFJakartaModule module;
	
	public NSFJakartaFileSystem(NSFJakartaModule module) {
		this.module = module;
		
		try (DesignCollectionIterator nav = new DesignCollectionIterator(module.getNotesDatabase())) {
			while (nav.hasNext()) {
				try(var entry = nav.next()) {

					if(NotesUtils.CmemflagTestMultiple(entry.flags(), NotesConstants.DFLAGPAT_FILE_WEB)) {

						// Add as a special class file entry as needed
						if (NotesUtils.CmemflagTestMultiple(entry.flags(), NotesConstants.DFLAGPAT_JAVAFILE)) {
							String classNamesCat = entry.classIndexItem();
							String[] classNames = StringUtil.splitString(classNamesCat, '|');
							for (int i = 0; i < classNames.length; i++) {
								if(classNames[i].length() > 7) {
									fileMap.put(classNames[i], new NSFMetadata(entry, FileType.JAVA_CLASS, "$ClassData" + i, entry.mimeType())); //$NON-NLS-1$
								}
							}
						}
						
						// Add as a normal file entry
						for(String name : sanitizeTitle(entry.title())) {
							if(NotesUtils.CmemflagTestMultiple(entry.flags(), NotesConstants.DFLAGPAT_JAVAJAR)) {
								name = "WEB-INF/lib/" + name; //$NON-NLS-1$
							}
							int noteId = entry.noteId();
							String fName = name;
							log.log(Level.DEBUG, () -> MessageFormat.format("Adding file element \"{0}\", note ID 0x{1}", fName, Integer.toHexString(noteId)));
							fileMap.put(name, new NSFMetadata(entry, FileType.FILE, entry.mimeType()));
						}
					} else if(NotesUtils.CmemflagTestMultiple(entry.flags(), NotesConstants.DFLAGPAT_SCRIPTLIB_JS)) {
						for(String name : sanitizeTitle(entry.title())) {
							int noteId = entry.noteId();
							log.log(Level.DEBUG, () -> MessageFormat.format("Adding JavaScript library \"{0}\", note ID 0x{1}", name, Integer.toHexString(noteId)));
							fileMap.put(name, new NSFMetadata(entry, FileType.JAVASCRIPT, entry.mimeType()));
						}
					} else if(NotesUtils.CmemflagTestMultiple(entry.flags(), NotesConstants.DFLAGPAT_IMAGE_RES_WEB)) {
						for(String name : sanitizeTitle(entry.title())) {
							int noteId = entry.noteId();
							log.log(Level.DEBUG, () -> MessageFormat.format("Adding image resource \"{0}\", note ID 0x{1}", name, Integer.toHexString(noteId)));
							fileMap.put(name, new NSFMetadata(entry, FileType.IMAGE, entry.mimeType()));
						}
					} else if(NotesUtils.CmemflagTestMultiple(entry.flags(), NotesConstants.DFLAGPAT_STYLE_SHEETS_WEB)) {
						for(String name : sanitizeTitle(entry.title())) {
							int noteId = entry.noteId();
							log.log(Level.DEBUG, () -> MessageFormat.format("Adding stylesheet \"{0}\", note ID 0x{1}", name, Integer.toHexString(noteId)));
							fileMap.put(name, new NSFMetadata(entry, FileType.STYLESHEET, entry.mimeType()));
						}
					}
					// TODO Re-add when figuring out how to read the value, or handle elsewhere
	//				else if(entry.getNoteClass() == NotesConstants.NOTE_CLASS_ICON) {
	//					int noteId = entry.getNoteID();
	//					if(log.isLoggable(Level.FINEST)) {
	//						log.finest(MessageFormat.format("Adding Icon note \"$Icon\", note ID 0x{0}", Integer.toHexString(noteId)));
	//					}
	//					fileMap.put("$Icon", noteId); //$NON-NLS-1$
	//				}

				}
			}
		}
	}
	
	@Override
	public Optional<FileEntry> getEntry(String res) {
		String path = ModuleUtil.trimResourcePath(res);
		NSFMetadata metadata = this.fileMap.get(path);
		if(metadata != null) {
			return Optional.of(new FileEntry(path, metadata));
		} else {
			return Optional.empty();
		}
	}
	
	@Override
	public Optional<URL> getUrl(String res) {
		if(this.fileMap.containsKey(res)) {
			try {
				// String scheme, String userInfo, String host, int port, String path, String query, String fragment
				URI uri = new URI(URLSCHEME, null, "localhost", 1352, '/' + module.getMapping().path() + '!' + res, null, null); //$NON-NLS-1$
				return Optional.of(uri.toURL());
			} catch(URISyntaxException | MalformedURLException e) {
				throw new RuntimeException(MessageFormat.format("Encountered exception constructing URL for resource \"{0}\" in {1}", res, module.getMapping().nsfPath()), e);
			}
		} else {
			return Optional.empty();
		}
	}
	
	@Override
	public Optional<URL> getWebResourceUrl(String res) {
		NSFMetadata meta = this.fileMap.get(res);
		if(meta != null && meta.webVisible()) {
			return getUrl(res);
		} else {
			return Optional.empty();
		}
	}

	@Override
	public Optional<InputStream> openStream(String res) {
		NSFMetadata noteData = this.fileMap.get(res);
		if(noteData != null) {
			try {
				NotesDatabase db = module.getNotesDatabase();
				if(!db.isValidHandle()) {
					throw new RuntimeException(MessageFormat.format("Unable to open database {0}", db.getDatabasePath()));
				}
				NotesNote note = db.openNote(noteData.noteId(), 0);
				if(note != null && note.isValidHandle()) {
					// TODO special handling for icon note
					String itemName = noteData.itemName();
					if(StringUtil.isNotEmpty(itemName)) {
						return Optional.of(FileAccess.readFileContentAsInputStream(note, itemName));
					} else {
						return Optional.of(FileAccess.readFileContentAsInputStream(note));
					}
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
	
	@Override
	public Stream<FileEntry> listFiles() {
		return listFiles(null);
	}

	@Override
	public Stream<FileEntry> listFiles(String basePath) {
		String path = ModuleUtil.trimResourcePath(basePath);
		boolean listAll = StringUtil.isEmpty(path);
		if(!listAll && !path.endsWith("/")) { //$NON-NLS-1$
			path += "/"; //$NON-NLS-1$
		}

		Stream<Map.Entry<String, NSFMetadata>> pathStream = fileMap.entrySet().stream();
		if(!listAll) {
			String fPath = path;
			pathStream = pathStream
				.filter(p -> p.getKey().startsWith(fPath) && p.getKey().indexOf('/', fPath.length()+1) == -1);
		}
		return pathStream.map(p -> new FileEntry(p.getKey(), p.getValue()));
	}
	
	@Override
	public URI buildURI(String path) throws URISyntaxException {
		String innerPath = path.startsWith("/") ? path : "/" + path; //$NON-NLS-1$ //$NON-NLS-2$
		return new URI(NSFJakartaFileSystem.URLSCHEME, null, "localhost", 1352, '/' + module.getMapping().path() + "!" + innerPath, null, null); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	private static List<String> sanitizeTitle(String title) {
		return Arrays.stream(StringUtil.splitString(title, '|'))
			.map(t -> t.replace('\\', '/'))
			.toList();
	}
	
	private static boolean isWebVisible(DesignEntry entry, FileType fileType) {
		boolean result = switch(fileType) {
			case FILE: {
				yield
					entry.flags().indexOf('Q') > -1 // DESIGN_FLAG_QUERY_FILTER (used in web-visible file-like entities)
					|| entry.flagsExt().indexOf('w') > -1; // DESIGN_FLAGEXT_WEBCONTENTFILE
			}
			case IMAGE: yield true;
			case JAVASCRIPT: yield true;
			case STYLESHEET: yield true;
			default: yield false;
		};
		return result;
	}
}
