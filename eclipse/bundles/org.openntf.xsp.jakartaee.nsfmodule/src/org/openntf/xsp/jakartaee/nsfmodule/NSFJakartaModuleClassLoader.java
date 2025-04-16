package org.openntf.xsp.jakartaee.nsfmodule;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.hcl.domino.module.nsf.NotesContext;
import com.hcl.domino.module.nsf.RuntimeFileSystem;
import com.ibm.commons.extension.ExtensionManager.ApplicationClassLoader;
import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.domino.xsp.module.nsf.NotesURL;
import com.ibm.xsp.library.LibraryServiceLoader;
import com.ibm.xsp.library.LibraryWrapper;
import org.openntf.xsp.jakartaee.nsfmodule.NSFJakartaModule.WithContext;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

public class NSFJakartaModuleClassLoader extends URLClassLoader implements ApplicationClassLoader {
	private static final Logger log = Logger.getLogger(NSFJakartaModuleClassLoader.class.getPackageName());
	
	private final NSFJakartaModule module;
	private final List<ClassLoader> extraDepends = new ArrayList<>();

	public NSFJakartaModuleClassLoader(NSFJakartaModule module) {
		super(MessageFormat.format("ClassLoader for {0}", module), createURLs(module), module.getClass().getClassLoader());
		
		this.module = module;
		
		// TODO build CodeSources for /WEB-INF/classes et al for defineClass calls
		// TODO probably a good idea to have a cross-instance object that
		//      keeps track of class names in "extraDepends" like the original ("libClasses")
		
		// Glean extraDepends
		Properties xspProperties = LibraryUtil.getXspProperties(module);
		String xspDependsProp = xspProperties.getProperty("xsp.library.depends", ""); //$NON-NLS-1$ //$NON-NLS-2$
		StringTokenizer xspDepends = new StringTokenizer(xspDependsProp, ","); //$NON-NLS-1$
		while(xspDepends.hasMoreElements()) {
			String libName = xspDepends.nextToken();
			if(StringUtil.isNotEmpty(libName)) {
				LibraryWrapper libWrapper = LibraryServiceLoader.getLibrary(libName);
				Objects.requireNonNull(libWrapper, MessageFormat.format("Unable to find library {0}", libName));
				extraDepends.add(libWrapper.getClassLoader());
			}
		}
		
		// TODO Add some things like IBM Commons and other frequently-used libraries?
	}

	@Override
	public Enumeration<URL> findApplicationResources(String path) throws IOException {
		// TODO figure out why DynamicClassLoader branches on hasJars - maybe performance?
		return super.findResources(path);
	}
	
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		
		try(WithContext ctx = module.withContext()) {
			// Check the current NSF for a file
			RuntimeFileSystem fs = this.module.getRuntimeFileSystem();
			String classFileName = MessageFormat.format("WEB-INF/classes/{0}.class", name.replace('.', '/')); //$NON-NLS-1$
			if(fs.exists(classFileName)) {
				byte[] bytecode = fs.getFileContentAsByteArray(NotesContext.getCurrent().getNotesDatabase(), classFileName);
				// TODO add CodeSource
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
		} catch (IOException e) {
			if(log.isLoggable(Level.WARNING)) {
				log.log(Level.WARNING, MessageFormat.format("Encountered exception closing class loader for {0}", this.module), e);
			}
		}
	}

	private static URL[] createURLs(NSFJakartaModule module) {
		String nsfPath = module.getMapping().nsfPath();
		
		try {
			List<URL> result = new ArrayList<>();
			
			result.add(NotesURL.createNSFUrl(nsfPath, "/WEB-INF/classes/")); //$NON-NLS-1$
			
			// Find all JARs
			RuntimeFileSystem fs = module.getRuntimeFileSystem();
			for(String path : fs.listJars().keySet()) {
				URL url = NotesURL.createNSFUrl(nsfPath, '/' + path);
				result.add(url);
			}
			
			return result.toArray(new URL[result.size()]);
		} catch (MalformedURLException e) {
			throw new UncheckedIOException(e);
		}
	}
}
