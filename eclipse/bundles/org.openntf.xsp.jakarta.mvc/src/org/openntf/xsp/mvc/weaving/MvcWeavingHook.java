package org.openntf.xsp.mvc.weaving;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.openntf.xsp.jakartaee.util.PriorityComparator;
import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;

/**
 * @since 3.0.0
 */
public class MvcWeavingHook implements WeavingHook {

	@Override
	public void weave(WovenClass c) {
		switch(c.getClassName()) {
		case "org.eclipse.krazo.util.ServiceLoaders" -> processServiceLoaders(c); //$NON-NLS-1$
		}
	}
	
	private void processServiceLoaders(WovenClass c) {
		CtClass cc = defrost(c, PriorityComparator.class);

		try {
			//  public static <T> List<T> list(Class<T> type)
			String body = """
			{
				java.util.List result = new java.util.ArrayList();
				
				ClassLoader classLoader = $1.getClassLoader();
				result.addAll(java.util.stream.StreamSupport.stream(java.util.ServiceLoader.load($1, classLoader).spliterator(), false).toList());
				
				classLoader = Thread.currentThread().getContextClassLoader();
	            result.addAll(java.util.stream.StreamSupport.stream(java.util.ServiceLoader.load($1, classLoader).spliterator(), false).toList());
	            
		        result.sort(org.openntf.xsp.jakartaee.util.PriorityComparator.DESCENDING);
		        return result;
			}"""; //$NON-NLS-1$
			CtMethod m = cc.getDeclaredMethod("list"); //$NON-NLS-1$
			m.setBody(body);
	        
			c.setBytes(cc.toBytecode());
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}
	
	private CtClass defrost(WovenClass c, Class<?>... contextClass) {
		ClassPool pool = new ClassPool();
		pool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
		pool.appendClassPath(new ClassClassPath(org.glassfish.hk2.osgiresourcelocator.ServiceLoader.class));
		for(Class<?> ctx : contextClass) {
			pool.appendClassPath(new ClassClassPath(ctx));
		}
		CtClass cc;
		try(InputStream is = new ByteArrayInputStream(c.getBytes())) {
			cc = pool.makeClass(is);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		cc.defrost();
		return cc;
	}
}
