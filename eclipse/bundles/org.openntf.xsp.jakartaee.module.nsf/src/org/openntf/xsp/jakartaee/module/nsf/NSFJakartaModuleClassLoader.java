package org.openntf.xsp.jakartaee.module.nsf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.commons.extension.ExtensionManager.ApplicationClassLoader;
import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesCollectionEntry;
import com.ibm.designer.domino.napi.NotesConstants;
import com.ibm.designer.domino.napi.NotesNote;
import com.ibm.designer.domino.napi.design.FileAccess;
import com.ibm.designer.domino.napi.util.NotesUtils;
import com.ibm.xsp.library.LibraryServiceLoader;
import com.ibm.xsp.library.LibraryWrapper;

import org.openntf.xsp.jakartaee.module.nsf.io.DesignCollectionIterator;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

public class NSFJakartaModuleClassLoader extends URLClassLoader implements ApplicationClassLoader {
	private record JavaClassNote(int noteId, String fileItem) {};
	
	private static final Logger log = Logger.getLogger(NSFJakartaModuleClassLoader.class.getPackageName());
	
	private final NSFJakartaModule module;
	private final List<ClassLoader> extraDepends = new ArrayList<>();
	private final Map<String, JavaClassNote> javaClasses = new HashMap<>();
	private final Map<String, Integer> jarFiles = new HashMap<>();
	private final Set<Path> cleanup = new HashSet<>();

	public NSFJakartaModuleClassLoader(NSFJakartaModule module) throws NotesAPIException {
		super(MessageFormat.format("ClassLoader for {0}", module), new URL[0], module.getClass().getClassLoader());
		
		this.module = module;
		
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
		
		// TODO Add some things like IBM Commons and other frequently-used libraries?
	}

	@Override
	public Enumeration<URL> findApplicationResources(String path) throws IOException {
		// TODO figure out why DynamicClassLoader branches on hasJars - maybe performance?
		return super.findResources(path);
	}
	
	public Set<String> getClassNames() {
		return this.javaClasses.keySet();
	}
	
	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		List<URL> result = new ArrayList<>();
		
		URL modRes = module.getResource(name);
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
		
		return Collections.enumeration(result);
	}
	
	@Override
	public URL getResource(String name) {
		try {
			URL modRes = module.getResource(name);
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
		
		return this.extraDepends.stream()
			.map(cl -> cl.getResource(name))
			.filter(Objects::nonNull)
			.findFirst()
			.orElse(null);
	}
	
	public URL getJarResource(String name) {
		return super.getResource(name);
	}
	
	@Override
	public InputStream getResourceAsStream(String name) {
		InputStream modRes = module.getResourceAsStream(name);
		if(modRes != null) {
			return modRes;
		}
		
		InputStream result = getJarResourceAsStream(name);
		if(result != null) {
			return result;
		}
		
		return this.extraDepends.stream()
			.map(cl -> cl.getResourceAsStream(name))
			.filter(Objects::nonNull)
			.findFirst()
			.orElse(null);
	}
	
	public InputStream getJarResourceAsStream(String name) {
		return super.getResourceAsStream(name);
	}
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		
		try {
			// Search for a Java file name to see if it has class data
			JavaClassNote javaClassNote = this.javaClasses.get(name);
			if(javaClassNote != null) {
				NotesNote javaNote = module.getNotesDatabase().openNote(javaClassNote.noteId(), 0);
				byte[] bytecode;
				try(InputStream is = FileAccess.readFileContentAsInputStream(javaNote, javaClassNote.fileItem())) {
					bytecode = is.readAllBytes();
				}
				// TODO add CodeSource?
				Class<?> clazz = this.defineClass(name, bytecode, 0, bytecode.length);
				return clazz;
			}
			
			// Check the current NSF for a class file - e.g. custom source folder
			String classFileName = String.format("WEB-INF/classes/%s.class", name.replace('.', '/')); //$NON-NLS-1$
			NotesNote classNote = FileAccess.getFileByPath(module.getNotesDatabase(), classFileName);
			if(classNote != null) {
				byte[] bytecode;
				try(InputStream is = FileAccess.readFileContentAsInputStream(classNote)) {
					bytecode = is.readAllBytes();
				}
				// TODO add CodeSource?
				Class<?> clazz = this.defineClass(name, bytecode, 0, bytecode.length);
				return clazz;
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
			throw new ClassNotFoundException(MessageFormat.format("Unable to locate class {0} in module {1}", name, module));
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
				log.log(Level.WARNING, MessageFormat.format("Encountered exception closing class loader for {0}", this.module), e);
			}
		}
	}
	
	private void findJavaElements() throws NotesAPIException, IOException {
		try (DesignCollectionIterator nav = new DesignCollectionIterator(this.module.getNotesDatabase())) {
			while (nav.hasNext()) {
				NotesCollectionEntry entry = (NotesCollectionEntry) nav.next();
				String flags = entry.getItemValueAsString(NotesConstants.DESIGN_FLAGS);

				// Look for Java resources
				if (NotesUtils.CmemflagTestMultiple(flags, NotesConstants.DFLAGPAT_JAVAFILE)) {
					String classNamesCat = entry.getItemValueAsString("$ClassIndexItem"); //$NON-NLS-1$
					String[] classNames = StringUtil.splitString(classNamesCat, '|');
					for (int i = 0; i < classNames.length; i++) {
						if(classNames[0].length() > 7) {
							int prefixLen = 16; // "WEB-INF/classes/"
							int suffixLen = 6; // ".class"
							String className = classNames[i].substring(prefixLen, classNames[i].length() - suffixLen);
							className = className.replace('/', '.');
							this.javaClasses.put(className, new JavaClassNote(entry.getNoteID(), "$ClassData" + i)); //$NON-NLS-1$
						}
					}
				} else if (NotesUtils.CmemflagTestMultiple(flags, NotesConstants.DFLAGPAT_JAVAJAR)) {
					String title = entry.getItemValueAsString(NotesConstants.FIELD_TITLE);
					String jarPath = "WEB-INF/lib/" + title; //$NON-NLS-1$
					jarFiles.put(jarPath, entry.getNoteID());

					// Add it to our base ClassLoader while here
					// TODO see if we can keep these in-memory only
					Path tempJar = Files.createTempFile(getClass().getSimpleName(), ".jar"); //$NON-NLS-1$
					NotesNote note = this.module.getNotesDatabase().openNote(entry.getNoteID(), 0);
					try(OutputStream os = Files.newOutputStream(tempJar, StandardOpenOption.TRUNCATE_EXISTING)) {
						FileAccess.readFileContent(note, os);
					} finally {
						note.recycle();
					}
					addURL(URI.create("jar:" + tempJar.toUri() + "!/").toURL());; //$NON-NLS-1$ //$NON-NLS-2$
//					URL url = NSFJakartaURL.of(module.getMapping().nsfPath(), '/' + jarPath);
//					addURL(url);
				} else if (NotesUtils.CmemflagTestMultiple(flags, NotesConstants.DFLAGPAT_FILE)) {
					String title = entry.getItemValueAsString(NotesConstants.FIELD_TITLE);
					// Assume anything in WEB-INF/classes ending with .class is fair game
					if (title.startsWith("WEB-INF/classes/") && title.endsWith(".class")) { //$NON-NLS-1$ //$NON-NLS-2$
						int prefixLen = 16; // "WEB-INF/classes/"
						int suffixLen = 6; // ".class"
						String className = title.substring(prefixLen, title.length() - suffixLen);
						className = className.replace('/', '.');
						this.javaClasses.put(className, new JavaClassNote(entry.getNoteID(), NotesConstants.ITEM_NAME_FILE_DATA));
					}
				}

				entry.recycle();
			}
		}
	}
}
