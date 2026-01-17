/**
 * Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakarta.mvc.weaving;

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
	public void weave(final WovenClass c) {
		switch(c.getClassName()) {
		case "org.eclipse.krazo.util.ServiceLoaders" -> processServiceLoaders(c); //$NON-NLS-1$
		}
	}

	private void processServiceLoaders(final WovenClass c) {
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

	private CtClass defrost(final WovenClass c, final Class<?>... contextClass) {
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
