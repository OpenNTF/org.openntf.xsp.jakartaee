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
package org.openntf.xsp.jakartaee.module.jakarta;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.xsp.library.LibraryServiceLoader;
import com.ibm.xsp.library.LibraryWrapper;

import org.openntf.xsp.jakartaee.module.ModuleClassLoaderExtender.ClassLoaderExtension;
import org.openntf.xsp.jakartaee.module.jakarta.ModuleFileSystem.FileEntry;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.jakartaee.util.ModuleUtil;

public class DefaultJakartaModuleClassLoader extends AbstractModuleClassLoader {
	private static final Logger log = Logger.getLogger(DefaultJakartaModuleClassLoader.class.getPackageName());
	
	private final List<ClassLoader> extraDepends = new ArrayList<>();
	private final Set<Path> cleanup = new HashSet<>();
	private final CodeSource nsfCodeSource;

	public DefaultJakartaModuleClassLoader(AbstractJakartaModule module) throws NotesAPIException, URISyntaxException, MalformedURLException {
		super(module, MessageFormat.format("{0}:{1}", AbstractJakartaModule.class.getSimpleName(), module.getModulePath().orElse("")), new URL[0], module.getClass().getClassLoader()); //$NON-NLS-2$
		
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
		
		URI uri = module.getRuntimeFileSystem().buildURI("/WEB-INF/classes"); //$NON-NLS-1$
		this.nsfCodeSource = new CodeSource(uri.toURL(), (CodeSigner[])null);
		
		// TODO Add some things like IBM Commons and other frequently-used libraries?
	}
	
	@Override
	public Set<String> getClassNames() {
		return this.getModule().getRuntimeFileSystem().listFiles()
			.map(FileEntry::name)
			.filter(p -> p.length() > 22 && p.startsWith("WEB-INF/classes/") && p.endsWith(".class")) //$NON-NLS-1$ //$NON-NLS-2$
			.map(DefaultJakartaModuleClassLoader::toClassName)
			.collect(Collectors.toSet());
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
		AbstractJakartaModule module = getModule();
		Optional<InputStream> modRes = module.getRuntimeFileSystem().openStream(ModuleUtil.trimResourcePath(name));
		if(modRes.isPresent()) {
			return modRes.get();
		} else {
			// If it's asking for a .class file, try with the prefix
			if(StringUtil.isNotEmpty(name) && name.endsWith(".class")) { //$NON-NLS-1$
				modRes = module.getRuntimeFileSystem().openStream(PathUtil.concat("WEB-INF/classes", name, '/')); //$NON-NLS-1$
				if(modRes.isPresent()) {
					return modRes.get();
				}
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
			Optional<InputStream> javaClassStream = getModule().getRuntimeFileSystem().openStream(toFileName(name));
			if(javaClassStream.isPresent()) {
				byte[] bytecode;
				try(InputStream is = javaClassStream.get()) {
					bytecode = is.readAllBytes();
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
		// Extract JARs to temp files for now
		ModuleFileSystem fs = this.getModule().getRuntimeFileSystem();
		fs.listFiles()
			.map(FileEntry::name)
			.filter(p -> p.length() > 16 && p.startsWith("WEB-INF/lib/") && p.endsWith(".jar")) //$NON-NLS-1$ //$NON-NLS-2$
			.map(fs::openStream)
			.forEach(optIs -> {
				try(InputStream is = optIs.get()) {
					Path tempJar = Files.createTempFile(getClass().getSimpleName(), ".jar"); //$NON-NLS-1$
					cleanup.add(tempJar);
					Files.copy(is, tempJar, StandardCopyOption.REPLACE_EXISTING);
					addURL(URI.create("jar:" + tempJar.toUri() + "!/").toURL()); //$NON-NLS-1$ //$NON-NLS-2$
				} catch (IOException e) {
					if(log.isLoggable(Level.WARNING)) {
						log.log(Level.WARNING, MessageFormat.format("Encountered exception processing JAR in {0}", this.getModule()), e);
					}
				}
			});
	}
	
	private static String toFileName(String className) {
		return String.format("WEB-INF/classes/%s.class", className.replace('.', '/')); //$NON-NLS-1$
	}
	
	private static String toClassName(String fileName) {
		int prefixLen = 16; // "WEB-INF/classes/"
		int suffixLen = 6; // ".class"
		String className = fileName.substring(prefixLen, fileName.length() - suffixLen);
		return className.replace('/', '.');
	}
}
