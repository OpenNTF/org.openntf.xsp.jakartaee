package org.openntf.xsp.jsonapi.weaving;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;

import jakarta.json.bind.Jsonb;
import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

/**
 * Forces the JSON-B provider to use Apache Johnzon, which is oddly not
 * picked up when running on Domino.
 * 
 * @since 2.9.0
 */
public class JsonbProviderWeavingHook implements WeavingHook {
	@Override
	public void weave(WovenClass c) {
		if("jakarta.json.bind.spi.JsonbProvider".equals(c.getClassName())) { //$NON-NLS-1$
			ClassPool pool = new ClassPool();
			pool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
			pool.appendClassPath(new ClassClassPath(Jsonb.class));
			CtClass cc;
			try(InputStream is = new ByteArrayInputStream(c.getBytes())) {
				cc = pool.makeClass(is);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			cc.defrost();
			try {
				CtMethod m = cc.getDeclaredMethod("provider", new CtClass[0]); //$NON-NLS-1$
				m.setBody("{ return (jakarta.json.bind.spi.JsonbProvider)Class.forName(\"org.apache.johnzon.jsonb.JohnzonProvider\").newInstance(); }"); //$NON-NLS-1$
				c.setBytes(cc.toBytecode());
			} catch(NotFoundException e) {
				// Then the method has been removed - that's fine
			} catch(CannotCompileException | IOException e) {
				new RuntimeException("Encountered exception when weaving jakarta.mail.util.FactoryFinder replacement", e).printStackTrace();
			}
		}
	}
}
