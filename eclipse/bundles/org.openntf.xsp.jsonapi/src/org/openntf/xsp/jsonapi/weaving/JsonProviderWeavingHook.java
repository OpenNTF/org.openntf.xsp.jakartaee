package org.openntf.xsp.jsonapi.weaving;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;

import jakarta.json.Json;
import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

/**
 * Forces the JSON-P provider to use Apache Johnzon, which is oddly not
 * picked up when running on Domino.
 * 
 * @since 2.9.0
 */
public class JsonProviderWeavingHook implements WeavingHook {
	@Override
	public void weave(WovenClass c) {
		if("jakarta.json.spi.JsonProvider".equals(c.getClassName())) { //$NON-NLS-1$
			ClassPool pool = new ClassPool();
			pool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
			pool.appendClassPath(new ClassClassPath(Json.class));
			CtClass cc;
			try(InputStream is = new ByteArrayInputStream(c.getBytes())) {
				cc = pool.makeClass(is);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			cc.defrost();
			try {
				CtMethod m = cc.getDeclaredMethod("provider", new CtClass[0]); //$NON-NLS-1$
				m.setBody("{ return (jakarta.json.spi.JsonProvider)Class.forName(\"org.apache.johnzon.core.JsonProviderImpl\").newInstance(); }"); //$NON-NLS-1$
				c.setBytes(cc.toBytecode());
			} catch(NotFoundException e) {
				// Then the method has been removed - that's fine
			} catch(CannotCompileException | IOException e) {
				new RuntimeException("Encountered exception when weaving jakarta.mail.util.FactoryFinder replacement", e).printStackTrace();
			}
		}
	}
}
