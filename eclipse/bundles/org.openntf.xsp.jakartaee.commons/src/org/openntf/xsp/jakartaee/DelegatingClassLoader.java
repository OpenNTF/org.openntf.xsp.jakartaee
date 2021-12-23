package org.openntf.xsp.jakartaee;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Basic implementation of a {@link ClassLoader} that delegates loading
 * to a series of delegates, in order.
 * 
 * @author Jesse Gallagher
 * @since 2.1.0
 */
public class DelegatingClassLoader extends ClassLoader {
	private final ClassLoader[] delegates;
	
	public DelegatingClassLoader(ClassLoader... delegates) {
		this.delegates = delegates;
	}
	
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		for(ClassLoader cl : delegates) {
			try {
				return cl.loadClass(name);
			} catch(ClassNotFoundException e) {
				// Ignore
			}
		}
		return super.loadClass(name);
	}
	
	@Override
	public URL getResource(String name) {
		for(ClassLoader cl : delegates) {
			URL res = cl.getResource(name);
			if(res != null) {
				return res;
			}
		}
		return super.getResource(name);
	}
	
	@Override
	public InputStream getResourceAsStream(String name) {
		for(ClassLoader cl : delegates) {
			InputStream is = cl.getResourceAsStream(name);
			if(is != null) {
				return is;
			}
		}
		return super.getResourceAsStream(name);
	}
	
	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		List<URL> result = new ArrayList<>();
		for(ClassLoader cl : delegates) {
			Enumeration<URL> res = cl.getResources(name);
			if(res != null) {
				result.addAll(Collections.list(res));
			}
		}
		return Collections.enumeration(result);
	}
}
