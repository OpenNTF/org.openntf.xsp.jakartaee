package org.openntf.xsp.nosql.weaving;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;

import jakarta.nosql.ValueReader;
import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

/**
 * This {@link WeavingHook} implementation listens for attempts to load 
 * {@code jakarta.nosql.ValueReaderDecorator} and, when found, replaces the
 * implementation methods with a version that dynamically looks up providers
 * at runtime instead of using a global singleton value.
 * 
 * @author Jesse Gallagher
 * @since 2.5.0
 */
public class NoSQLWeavingHook implements WeavingHook {

	@SuppressWarnings("nls")
	@Override
	public void weave(WovenClass c) {
		if("jakarta.nosql.ValueReaderDecorator".equals(c.getClassName())) { //$NON-NLS-1$
			ClassPool pool = new ClassPool();
			pool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
			pool.appendClassPath(new ClassClassPath(ValueReader.class));
			CtClass cc;
			try(InputStream is = new ByteArrayInputStream(c.getBytes())) {
				cc = pool.makeClass(is);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			cc.defrost();

			try {
				// boolean test(Class clazz)
				{
					String body = "{\n"
							+ "		java.util.List readers = jakarta.nosql.ServiceLoaderProvider.getSupplierStream(jakarta.nosql.ValueReader.class).collect(java.util.stream.Collectors.toList());\n"
							+ "		for(int i = 0; i < readers.size(); i++) {\n"
							+ "			jakarta.nosql.ValueReader r = (jakarta.nosql.ValueReader)readers.get(i);\n"
							+ "			if(r.test($1)) {\n"
							+ "				return true;\n"
							+ "			}\n"
							+ "		}\n"
							+ "		return false;\n"
							+ "    }";
					CtMethod m = cc.getDeclaredMethod("test"); //$NON-NLS-1$
					m.setBody(body);
				}
				
				// <T> T read(Class<T> clazz, Object value)
				{
					String body = "{\n"
							+ "        if ($1.isInstance($2)) {\n"
							+ "            return $1.cast($2);\n"
							+ "        }\n"
							+ "        java.util.List readers = jakarta.nosql.ServiceLoaderProvider.getSupplierStream(jakarta.nosql.ValueReader.class).collect(java.util.stream.Collectors.toList());\n"
							+ "        for(int i = 0; i < readers.size(); i++) {\n"
							+ "			jakarta.nosql.ValueReader r = (jakarta.nosql.ValueReader)readers.get(i);\n"
							+ "			if(r.test($1)) {\n"
							+ "				return ((jakarta.nosql.ValueReader)r).read($1, $2);\n"
							+ "			}\n"
							+ "		}\n"
							+ "        throw new UnsupportedOperationException(\"The type \" + $1 + \" is not supported yet\");\n"
							+ "    }";
					CtMethod m = cc.getDeclaredMethod("read"); //$NON-NLS-1$
					m.setBody(body);
				}
			
				c.setBytes(cc.toBytecode());
			} catch(NotFoundException e) {
				// Then the method has been removed - that's fine
				e.printStackTrace();
			} catch(CannotCompileException | IOException e) {
				e.printStackTrace();
				new RuntimeException("Encountered exception when weaving jakarta.nosql.ServiceLoaderProvider replacement", e).printStackTrace();
			} catch(Throwable t) {

				t.printStackTrace();
			}
		}
	}
}
