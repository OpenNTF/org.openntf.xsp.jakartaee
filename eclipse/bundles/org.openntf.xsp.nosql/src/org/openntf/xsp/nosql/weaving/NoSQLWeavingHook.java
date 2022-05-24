package org.openntf.xsp.nosql.weaving;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

/**
 * This {@link WeavingHook} implementation listens for attempts to load 
 * {@code jakarta.nosql.ServiceLoaderProvider} and, when found, replaces the
 * {@code getSupplierStream} method with a version that uses the requested
 * class's ClassLoader as the context.
 * 
 * @author Jesse Gallagher
 * @since 2.5.0
 */
public class NoSQLWeavingHook implements WeavingHook {

	@Override
	public void weave(WovenClass c) {
		if("jakarta.nosql.ServiceLoaderProvider".equals(c.getClassName())) { //$NON-NLS-1$
			ClassPool pool = new ClassPool();
			pool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
			CtClass cc;
			try(InputStream is = new ByteArrayInputStream(c.getBytes())) {
				cc = pool.makeClass(is);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			cc.defrost();
			
			String body = "{ java.util.Spliterator iter = java.util.ServiceLoader.load($1, $1.getClassLoader()).spliterator();\n" //$NON-NLS-1$
			            + "return java.util.stream.StreamSupport.stream(iter, false); }"; //$NON-NLS-1$
			
			try {
				CtMethod m = cc.getDeclaredMethod("getSupplierStream"); //$NON-NLS-1$
				m.setBody(body);
				c.setBytes(cc.toBytecode());
			} catch(NotFoundException e) {
				// Then the method has been removed - that's fine
				e.printStackTrace();
			} catch(CannotCompileException | IOException e) {
				e.printStackTrace();
				new RuntimeException("Encountered exception when weaving ClassLoaderUtil replacement", e).printStackTrace();
			} catch(Throwable t) {

				t.printStackTrace();
			}
		}
	}
}
