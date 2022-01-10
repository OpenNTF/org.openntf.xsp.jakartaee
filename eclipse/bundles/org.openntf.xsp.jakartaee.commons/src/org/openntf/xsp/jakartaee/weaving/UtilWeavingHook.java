package org.openntf.xsp.jakartaee.weaving;

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
 * {@code com.ibm.xsp.util.ClassLoaderUtil} and, when found, replaces the
 * {@code checkProhibitedClassNames} method with a no-op version, in order to
 * allow loading of otherwise-prohibited packages (like Eclipse MicroProfile)
 * from within an NSF.
 * 
 * @author Jesse Gallagher
 * @since 2.2.0
 */
public class UtilWeavingHook implements WeavingHook {

	@Override
	public void weave(WovenClass c) {
		if("com.ibm.xsp.util.ClassLoaderUtil".equals(c.getClassName())) { //$NON-NLS-1$
			ClassPool pool = new ClassPool();
			pool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
			CtClass cc;
			try(InputStream is = new ByteArrayInputStream(c.getBytes())) {
				cc = pool.makeClass(is);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			cc.defrost();
			try {
				CtMethod m = cc.getDeclaredMethod("checkProhibitedClassNames"); //$NON-NLS-1$
				m.setBody("{ return false; }"); //$NON-NLS-1$
				c.setBytes(cc.toBytecode());
			} catch(NotFoundException e) {
				// Then the method has been removed - that's fine
			} catch(CannotCompileException | IOException e) {
				new RuntimeException("Encountered exception when weaving ClassLoaderUtil replacement", e).printStackTrace();
			}
		}
	}

}
