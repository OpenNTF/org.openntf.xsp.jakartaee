/**
 * Copyright (c) 2018-2023 Contributors to the XPages Jakarta EE Support Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.xsp.nosql.weaving;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;

import org.eclipse.jnosql.communication.TypeReferenceReader;
import org.eclipse.jnosql.communication.ValueReader;
import org.eclipse.jnosql.communication.ValueWriter;
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

	@Override
	public void weave(WovenClass c) {
		if("jakarta.nosql.ValueReaderDecorator".equals(c.getClassName())) { //$NON-NLS-1$
			processValueReader(c);
		} else if("org.eclipse.jnosql.communication.writer.ValueWriterDecorator".equals(c.getClassName())) { //$NON-NLS-1$
			// TODO ValueWriter has static <T, S> Stream<ValueWriter<T, S>> getWriters()
			processValueWriter(c);
		} else if("jakarta.nosql.TypeReferenceReaderDecorator".equals(c.getClassName())) { //$NON-NLS-1$
			processTypeReferenceReader(c);
		}
	}

	@SuppressWarnings("nls")
	private void processValueReader(WovenClass c) {
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
						+ "				return r.read($1, $2);\n"
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
	
	@SuppressWarnings("nls")
	private void processTypeReferenceReader(WovenClass c) {
		ClassPool pool = new ClassPool();
		pool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
		pool.appendClassPath(new ClassClassPath(TypeReferenceReader.class));
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
						+ "		java.util.List readers = jakarta.nosql.ServiceLoaderProvider.getSupplierStream(jakarta.nosql.TypeReferenceReader.class).collect(java.util.stream.Collectors.toList());\n"
						+ "		for(int i = 0; i < readers.size(); i++) {\n"
						+ "			jakarta.nosql.TypeReferenceReader r = (jakarta.nosql.TypeReferenceReader)readers.get(i);\n"
						+ "			if(r.test($1)) {\n"
						+ "				return true;\n"
						+ "			}\n"
						+ "		}\n"
						+ "		return false;\n"
						+ "    }";
				CtMethod m = cc.getDeclaredMethod("test"); //$NON-NLS-1$
				m.setBody(body);
			}
			
			// <T> T convert(TypeSupplier, Object value)
			{
				String body = "{\n"
						+ "        java.util.List readers = jakarta.nosql.ServiceLoaderProvider.getSupplierStream(jakarta.nosql.TypeReferenceReader.class).collect(java.util.stream.Collectors.toList());\n"
						+ "        for(int i = 0; i < readers.size(); i++) {\n"
						+ "			jakarta.nosql.TypeReferenceReader r = (jakarta.nosql.TypeReferenceReader)readers.get(i);\n"
						+ "			if(r.test($1)) {\n"
						+ "				return r.convert($1, $2);\n"
						+ "			}\n"
						+ "		}\n"
						+ "        throw new UnsupportedOperationException(\"The type \" + $1 + \" is not supported yet\");\n"
						+ "    }";
				CtMethod m = cc.getDeclaredMethod("convert"); //$NON-NLS-1$
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
	
	@SuppressWarnings("nls")
	private void processValueWriter(WovenClass c) {
		ClassPool pool = new ClassPool();
		pool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
		pool.appendClassPath(new ClassClassPath(ValueWriter.class));
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
						+ "		java.util.List writers = jakarta.nosql.ServiceLoaderProvider.getSupplierStream(jakarta.nosql.ValueWriter.class).collect(java.util.stream.Collectors.toList());\n"
						+ "		for(int i = 0; i < writers.size(); i++) {\n"
						+ "			jakarta.nosql.ValueWriter w = (jakarta.nosql.ValueWriter)writers.get(i);\n"
						+ "			if(w.test($1)) {\n"
						+ "				return true;\n"
						+ "			}\n"
						+ "		}\n"
						+ "		return false;\n"
						+ "    }";
				CtMethod m = cc.getDeclaredMethod("test"); //$NON-NLS-1$
				m.setBody(body);
			}
			
			// Object write(Object value)
			{
				String body = "{\n"
						+ "        Class clazz = $1.getClass();\n"
						+ "        java.util.List writers = jakarta.nosql.ServiceLoaderProvider.getSupplierStream(jakarta.nosql.ValueWriter.class).collect(java.util.stream.Collectors.toList());\n"
						+ "        for(int i = 0; i < writers.size(); i++) {\n"
						+ "			jakarta.nosql.ValueWriter w = (jakarta.nosql.ValueWriter)writers.get(i);\n"
						+ "			if(w.test($1)) {\n"
						+ "				return w.write($1);\n"
						+ "			}\n"
						+ "		}\n"
						+ "        throw new UnsupportedOperationException(\"The type \" + clazz + \" is not supported yet\");\n"
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
