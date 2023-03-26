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
package org.openntf.xsp.cdi.util;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.weld.bean.proxy.util.WeldDefaultProxyServices;
import org.jboss.weld.serialization.spi.ProxyServices;
import org.openntf.xsp.jakartaee.DelegatingClassLoader;

import com.ibm.domino.xsp.module.nsf.ModuleClassLoader;

/**
 * This subclass of {@link WeldDefaultProxyServices} keeps an internal cache of generated
 * classes. This is to avoid {@code java.lang.LinkageError: A duplicate class definition for jakarta/enterprise/event/Event$WeldEvent$Proxy$_$$_Weld$Proxy$ is found}.
 * 
 * <p>This implementation also provides a delegating classloader when defining classes,
 * which allows for system-level classes to be represented by {@code Bean} classes.
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public class NSFProxyServices extends WeldDefaultProxyServices {

    private static java.lang.reflect.Method defineClass1, defineClass2;
    private static final AtomicBoolean classLoaderMethodsMadeAccessible = new AtomicBoolean(false);
	
	private static Map<String, Class<?>> classCache = Collections.synchronizedMap(new HashMap<>());

    /**
     * This method cracks open {@code ClassLoader#defineClass()} methods by calling {@code setAccessible()}.
     * <p>
     * It is invoked during {@code WeldStartup#startContainer()} and only in case the integrator does not
     * fully implement {@link ProxyServices}.
     **/
    public static void makeClassLoaderMethodsAccessible() {
        // the AtomicBoolean make sure this gets invoked only once as WeldStartup is triggered per deployment
        if (classLoaderMethodsMadeAccessible.compareAndSet(false, true)) {
            try {
                AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {
                    public Object run() throws Exception {
                        Class<?> cl = Class.forName("java.lang.ClassLoader");
                        final String name = "defineClass";

                        defineClass1 = cl.getDeclaredMethod(name, String.class, byte[].class, int.class, int.class);
                        defineClass2 = cl.getDeclaredMethod(name, String.class, byte[].class, int.class, int.class, ProtectionDomain.class);
                        defineClass1.setAccessible(true);
                        defineClass2.setAccessible(true);
                        return null;
                    }
                });
            } catch (PrivilegedActionException pae) {
                throw new RuntimeException("cannot initialize ClassPool", pae.getException());
            }
        }
    }
    
    public NSFProxyServices() {
		makeClassLoaderMethodsAccessible();
	}
	
	@Override
	public Class<?> defineClass(Class<?> originalClass, String className, byte[] classBytes, int off, int len)
			throws ClassFormatError {
		return defineClass(originalClass, className, classBytes, off, len, null);
	}
	
	@Override
	public Class<?> defineClass(Class<?> originalClass, String className, byte[] classBytes, int off, int len,
			ProtectionDomain protectionDomain) throws ClassFormatError {
		Class<?> result = doDefineClass(originalClass, className, classBytes, off, len, protectionDomain);
		if(result != null && !(originalClass.getClassLoader() instanceof ModuleClassLoader)) {
			classCache.put(className + originalClass.hashCode(), result);
		}
		return result;
	}
	
	@Override
	public synchronized Class<?> loadClass(Class<?> originalClass, String classBinaryName) throws ClassNotFoundException {
		String key = classBinaryName + originalClass.hashCode();
		if(classCache.containsKey(key)) {
			return classCache.get(key);
		}
		return super.loadClass(originalClass, classBinaryName);
	}
	
	private Class<?> doDefineClass(Class<?> originalClass, String className, byte[] classBytes, int off, int len, ProtectionDomain protectionDomain) throws ClassFormatError {
        try {
            java.lang.reflect.Method method;
            Object[] args;
            if (protectionDomain == null) {
                method = defineClass1;
                args = new Object[]{className, classBytes, 0, len};
            } else {
                method = defineClass2;
                args = new Object[]{className, classBytes, 0, len, protectionDomain};
            }
            ClassLoader loader = new DelegatingClassLoader(originalClass.getClassLoader(), getClass().getClassLoader(), Thread.currentThread().getContextClassLoader());
            @SuppressWarnings("rawtypes")
			Class<?> clazz = (Class) method.invoke(loader, args);
            return clazz;
        } catch (RuntimeException e) {
            throw e;
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
