package org.openntf.xsp.nosql.communication.driver.lsxbe.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * This subclass of {@link ObjectInputStream} attempts to load
 * classes from the current context class loader.
 * 
 * @author Jesse Gallagher
 * @since 2.6.0
 */
public class LoaderObjectInputStream extends ObjectInputStream {

	public LoaderObjectInputStream(InputStream in) throws IOException {
		super(in);
	}
	
	@Override
	protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
		try {
			return Class.forName(desc.getName(), true, Thread.currentThread().getContextClassLoader());
		} catch(ClassNotFoundException e) {
			return super.resolveClass(desc);	
		}
	}

}
