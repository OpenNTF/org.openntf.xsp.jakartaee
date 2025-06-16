package org.openntf.xsp.jakartaee.module.jakarta;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Set;

import com.ibm.commons.extension.ExtensionManager.ApplicationClassLoader;

/**
 * @since 3.5.0
 */
public abstract class AbstractModuleClassLoader extends URLClassLoader implements ApplicationClassLoader {
	public AbstractModuleClassLoader(String name, URL[] urls, ClassLoader parent) {
		super(name, urls, parent);
	}

	@Override
	public Enumeration<URL> findApplicationResources(String path) throws IOException {
		// TODO figure out why DynamicClassLoader branches on hasJars - maybe performance?
		return super.findResources(path);
	}

	public InputStream getJarResourceAsStream(String name) {
		return super.getResourceAsStream(name);
	}

	public URL getJarResource(String name) {
		return super.getResource(name);
	}
	
	public abstract Set<String> getClassNames();
}