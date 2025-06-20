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
package org.openntf.xsp.jakartaee.module.jakartansf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesConstants;
import com.ibm.designer.domino.napi.NotesNote;
import com.ibm.designer.domino.napi.design.FileAccess;
import com.ibm.designer.domino.napi.util.NotesUtils;
import com.ibm.xsp.library.LibraryServiceLoader;
import com.ibm.xsp.library.LibraryWrapper;

import org.openntf.xsp.jakartaee.module.ModuleClassLoaderExtender.ClassLoaderExtension;
import org.openntf.xsp.jakartaee.module.jakarta.AbstractModuleClassLoader;
import org.openntf.xsp.jakartaee.module.jakartansf.io.DesignCollectionIterator;
import org.openntf.xsp.jakartaee.module.jakartansf.io.NSFJakartaFileSystem;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

public class NSFJakartaModuleClassLoader extends AbstractModuleClassLoader {
	private record JavaClassNote(int noteId, String fileItem) {}
	
	private static final Logger log = Logger.getLogger(NSFJakartaModuleClassLoader.class.getPackageName());
	
	private final List<ClassLoader> extraDepends = new ArrayList<>();
	private final Map<String, JavaClassNote> javaClasses = new HashMap<>();
	private final Map<String, JavaClassNote> javaClassFiles = new HashMap<>();
	private final Map<String, Integer> jarFiles = new HashMap<>();
	private final Set<Path> cleanup = new HashSet<>();
	private final CodeSource nsfCodeSource;

	public NSFJakartaModuleClassLoader(NSFJakartaModule module) throws NotesAPIException, URISyntaxException, MalformedURLException {
		super(module, MessageFormat.format("{0}:{1}", NSFJakartaModule.class.getSimpleName(), module.getMapping().path()), new URL[0], module.getClass().getClassLoader());
		
		// TODO build CodeSources for /WEB-INF/classes et al for defineClass calls
		try {
			findJavaElements();
		} catch(IOException e) {
			throw new UncheckedIOException(e);
		}
		
		// Glean extraDepends
		Properties xspProperties = LibraryUtil.getXspProperties(module);
		String xspDependsProp = xspProperties.getProperty("xsp.library.depends", ""); //$NON-NLS-1$ //$NON-NLS-2$
		StringTokenizer xspDepends = new StringTokenizer(xspDependsProp, ","); //$NON-NLS-1$
		while(xspDepends.hasMoreElements()) {
			String libName = xspDepends.nextToken();
			if(StringUtil.isNotEmpty(libName)) {
				LibraryWrapper libWrapper = LibraryServiceLoader.getLibrary(libName);
				Objects.requireNonNull(libWrapper, MessageFormat.format("Unable to find library {0}", libName));
				if(!libWrapper.isGlobalScope()) {
					extraDepends.add(libWrapper.getClassLoader());
				}
			}
		}
		
		URI uri = new URI(NSFJakartaFileSystem.URLSCHEME, null, "localhost", 1352, '/' + module.getMapping().path() + "!/WEB-INF/classes", null, null); //$NON-NLS-1$ //$NON-NLS-2$
		this.nsfCodeSource = new CodeSource(uri.toURL(), (CodeSigner[])null);
		
		// TODO Add some things like IBM Commons and other frequently-used libraries?
	}
	
	@Override
	public NSFJakartaModule getModule() {
		return (NSFJakartaModule)super.getModule();
	}
	
	@Override
	public Set<String> getClassNames() {
		return this.javaClasses.keySet();
	}
	
	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		List<URL> result = new ArrayList<>();
		
		URL modRes = getModule().getResource(name);
		if(modRes != null) {
			result.add(modRes);
		}
		// TODO determine if this is ever needed
//		modRes = module.getResource(PathUtil.concat("WEB-INF/classes", name, '/')); //$NON-NLS-1$
//		if(modRes != null) {
//			result.add(modRes);
//		}
		
		result.addAll(Collections.list(super.findResources(name)));
		
		for(ClassLoader cl : this.extraDepends) {
			result.addAll(Collections.list(cl.getResources(name)));
		}
		
		for(ClassLoaderExtension extension : getExtensions()) {
			result.addAll(extension.getResources(name));
		}
		
		return Collections.enumeration(result);
	}
	
	@Override
	public URL getResource(String name) {
		try {
			URL modRes = getModule().getResource(name);
			if(modRes != null) {
				return modRes;
			}
		} catch (MalformedURLException e) {
			// TODO ignore
		}
		
		URL result = getJarResource(name);
		if(result != null) {
			return result;
		}
		
		result = this.extraDepends.stream()
			.map(cl -> cl.getResource(name))
			.filter(Objects::nonNull)
			.findFirst()
			.orElse(null);
		
		if(result != null) {
			return result;
		}
		
		return getExtensions().stream()
			.map(ext -> ext.getResource(name))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.findFirst()
			.orElse(null);
	}
	
	@Override
	public InputStream getResourceAsStream(String name) {
		NSFJakartaModule module = getModule();
		InputStream modRes = module.getResourceAsStream(name);
		if(modRes != null) {
			return modRes;
		}
		
		// The above won't serve up .class files, but we should
		JavaClassNote javaClassNote = javaClassFiles.get(PathUtil.concat("WEB-INF/classes", name, '/')); //$NON-NLS-1$
		if(javaClassNote != null) {
			try {
				NotesNote javaNote = module.getNotesDatabase().openNote(javaClassNote.noteId(), 0);
				try {
					return FileAccess.readFileContentAsInputStream(javaNote, javaClassNote.fileItem());
				} finally {
					javaNote.recycle();
				}
			} catch(NotesAPIException e) {
				throw new RuntimeException(e);
			}
		}
		
		InputStream result = getJarResourceAsStream(name);
		if(result != null) {
			return result;
		}
		
		result = this.extraDepends.stream()
			.map(cl -> cl.getResourceAsStream(name))
			.filter(Objects::nonNull)
			.findFirst()
			.orElse(null);
		
		if(result != null) {
			return result;
		}

		return getExtensions().stream()
			.map(ext -> ext.getResourceAsStream(name))
			.filter(Optional::isPresent)
			.map(Optional::get)
			.findFirst()
			.orElse(null);
	}
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		for(var ext : getExtensions()) {
			Optional<Class<?>> c = ext.loadClass(name);
			if(c.isPresent()) {
				return c.get();
			}
		}
		
		try {
			// Search for a Java file name to see if it has class data
			JavaClassNote javaClassNote = this.javaClasses.get(name);
			if(javaClassNote != null) {
				NotesNote javaNote = getModule().getNotesDatabase().openNote(javaClassNote.noteId(), 0);
				byte[] bytecode;
				try {
					try(InputStream is = FileAccess.readFileContentAsInputStream(javaNote, javaClassNote.fileItem())) {
						bytecode = is.readAllBytes();
					}
				} finally {
					javaNote.recycle();
				}
				return this.defineClass(name, bytecode, 0, bytecode.length, this.nsfCodeSource);
			}
			
			// Next, check the parent ClassLoader
			try {
				Class<?> clazz = super.findClass(name);
				return clazz;
			} catch(ClassNotFoundException e) {
				// Ignore
			}
			
			// Check extraDepends
			for(ClassLoader cl : this.extraDepends) {
				try {
					// TODO cache these loaded classes by name across all instances
					Class<?> clazz = cl.loadClass(name);
					return clazz;
				} catch(ClassNotFoundException e) {
					// Ignore
				}
			}
			
			// Finally, throw up our hands
			throw new ClassNotFoundException(MessageFormat.format("Unable to locate class {0} in module {1}", name, getModule()));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		} catch (NotesAPIException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void close() {
		try {
			super.close();
			
			for(Path path : this.cleanup) {
				Files.deleteIfExists(path);
			}
			this.cleanup.clear();
		} catch (IOException e) {
			if(log.isLoggable(Level.WARNING)) {
				log.log(Level.WARNING, MessageFormat.format("Encountered exception closing class loader for {0}", this.getModule()), e);
			}
		}
	}
	
	private void findJavaElements() throws NotesAPIException, IOException {
		try (DesignCollectionIterator nav = new DesignCollectionIterator(getModule().getNotesDatabase())) {
			while (nav.hasNext()) {
				try(var entry = nav.next()) {
					// Look for Java resources
					if (NotesUtils.CmemflagTestMultiple(entry.flags(), NotesConstants.DFLAGPAT_JAVAFILE)) {
						String classNamesCat = entry.classIndexItem();
						String[] classNames = StringUtil.splitString(classNamesCat, '|');
						for (int i = 0; i < classNames.length; i++) {
							if(classNames[i].length() > 7) {
								int prefixLen = 16; // "WEB-INF/classes/"
								int suffixLen = 6; // ".class"
								String className = classNames[i].substring(prefixLen, classNames[i].length() - suffixLen);
								className = className.replace('/', '.');
								JavaClassNote javaNote = new JavaClassNote(entry.noteId(), "$ClassData" + i); //$NON-NLS-1$
								this.javaClasses.put(className, javaNote);
								this.javaClassFiles.put(classNames[i], javaNote);
								
							}
						}
					} else if (NotesUtils.CmemflagTestMultiple(entry.flags(), NotesConstants.DFLAGPAT_JAVAJAR)) {
						String title = entry.title();
						String jarPath = "WEB-INF/lib/" + title; //$NON-NLS-1$
						jarFiles.put(jarPath, entry.noteId());
	
						// Add it to our base ClassLoader while here
						// TODO see if we can keep these in-memory only
						Path tempJar = extractJar(entry);
						addURL(URI.create("jar:" + tempJar.toUri() + "!/").toURL()); //$NON-NLS-1$ //$NON-NLS-2$
					} else if (NotesUtils.CmemflagTestMultiple(entry.flags(), NotesConstants.DFLAGPAT_FILE)) {
						String title = entry.title();
						// Assume anything in WEB-INF/classes ending with .class is fair game
						if (title.startsWith("WEB-INF/classes/") && title.endsWith(".class")) { //$NON-NLS-1$ //$NON-NLS-2$
							int prefixLen = 16; // "WEB-INF/classes/"
							int suffixLen = 6; // ".class"
							String className = title.substring(prefixLen, title.length() - suffixLen);
							className = className.replace('/', '.');
							this.javaClasses.put(className, new JavaClassNote(entry.noteId(), NotesConstants.ITEM_NAME_FILE_DATA));
						} else if(title.startsWith("WEB-INF/lib/") && title.endsWith(".jar")) { //$NON-NLS-1$ //$NON-NLS-2$
							// There may also be "loose" JARs in the conceptual WEB-INF/lib
							Path tempJar = extractJar(entry);
							addURL(URI.create("jar:" + tempJar.toUri() + "!/").toURL()); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}

				}
			}
		}
	}
	
	private Path extractJar(DesignCollectionIterator.DesignEntry entry) throws IOException, NotesAPIException {
		Path tempJar = Files.createTempFile(getClass().getSimpleName(), ".jar"); //$NON-NLS-1$
		cleanup.add(tempJar);
		NotesNote note = getModule().getNotesDatabase().openNote(entry.noteId(), 0);
		try(OutputStream os = Files.newOutputStream(tempJar, StandardOpenOption.TRUNCATE_EXISTING)) {
			FileAccess.readFileContent(note, os);
		} finally {
			note.recycle();
		}
		return tempJar;
	}
}
