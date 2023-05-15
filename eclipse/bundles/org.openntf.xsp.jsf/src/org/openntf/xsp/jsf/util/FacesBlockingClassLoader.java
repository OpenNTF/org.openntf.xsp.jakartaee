package org.openntf.xsp.jsf.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import jakarta.faces.context.FacesContext;

/**
 * This {@link ClassLoader} implementation blocks access from a JSF environment
 * to the XPages implementation classes. Additionally, it allows access to
 * META-INF resources from the Faces API bundle, which would otherwise
 * be invisible in OSGi.
 * 
 * @author Jesse Gallagher
 * @since 2.12.0
 */
public class FacesBlockingClassLoader extends URLClassLoader {
	private final ClassLoader facesCl;

	public FacesBlockingClassLoader(URL[] urls, ClassLoader parent) {
		super(urls, parent);
		this.facesCl = FacesContext.class.getClassLoader();
	}
	
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if (name != null && name.startsWith("com.sun.faces.")) { //$NON-NLS-1$
			throw new ClassNotFoundException();
		}
		return super.loadClass(name);
	}

	// The Faces API will already be on the classpath, but its resources won't be.
	//   Also shim in access to that
	
	@Override
	public URL getResource(String name) {
		URL parent = super.getResource(name);
		if(parent != null) {
			return parent;
		}
		return facesCl.getResource(name);
	}
	
	@Override
	public InputStream getResourceAsStream(String name) {
		InputStream parent = super.getResourceAsStream(name);
		if(parent != null) {
			return parent;
		}
		return facesCl.getResourceAsStream(name);
	}
	
	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		List<URL> parent = Collections.list(super.getResources(name));
		parent.addAll(Collections.list(facesCl.getResources(name)));
		return Collections.enumeration(parent);
	}

}
