package org.openntf.xsp.jakarta.rest.weaving;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;

public class RestWeavingHook implements WeavingHook {

	@Override
	public void weave(WovenClass c) {
		switch(c.getClassName()) {
			case "org.jboss.resteasy.spi.Registry" -> processRegistry(c); //$NON-NLS-1$
		}
	}
	
	@SuppressWarnings("nls")
	private void processRegistry(WovenClass c) {

		ClassPool pool = new ClassPool();
		pool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
		pool.appendClassPath(new LoaderClassPath(c.getBundleWiring().getClassLoader()));
		CtClass cc;
		try(InputStream is = new ByteArrayInputStream(c.getBytes())) {
			cc = pool.makeClass(is);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		cc.defrost();
		
		try {
			// Registry of(final ResteasyProviderFactory providerFactory)
			{
				String body = """
			    {
					Class clazz = org.eclipse.core.runtime.Platform.getBundle("org.openntf.org.jboss.resteasy.cdi").loadClass("org.jboss.resteasy.cdi.CdiAwareRegistry");
					Class facClass = Class.forName("org.jboss.resteasy.spi.ResteasyProviderFactory");
					return clazz.getConstructors()[0].newInstance(new Object[] { $1 });
		        }""";
				CtMethod m = cc.getDeclaredMethod("of"); //$NON-NLS-1$
				m.setBody(body);
			}

			c.setBytes(cc.toBytecode());
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}
}
