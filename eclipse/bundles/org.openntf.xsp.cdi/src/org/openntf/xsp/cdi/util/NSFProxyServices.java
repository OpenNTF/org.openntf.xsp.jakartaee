/**
 * Copyright © 2018-2022 Contributors to the XPages Jakarta EE Support Project
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

import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.weld.bean.proxy.util.WeldDefaultProxyServices;

import com.ibm.domino.xsp.module.nsf.ModuleClassLoader;

/**
 * This subclass of {@link WeldDefaultProxyServices} keeps an internal cache of generated
 * classes. This is to avoid {@code java.lang.LinkageError: A duplicate class definition for jakarta/enterprise/event/Event$WeldEvent$Proxy$_$$_Weld$Proxy$ is found}
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public class NSFProxyServices extends WeldDefaultProxyServices {
	
	private static Map<String, Class<?>> classCache = Collections.synchronizedMap(new HashMap<>());
	
	@Override
	public Class<?> defineClass(Class<?> originalClass, String className, byte[] classBytes, int off, int len)
			throws ClassFormatError {
		Class<?> result = super.defineClass(originalClass, className, classBytes, off, len);
		if(result != null && !(originalClass.getClassLoader() instanceof ModuleClassLoader)) {
			classCache.put(className + originalClass.hashCode(), result);
		}
		return result;
	}
	
	@Override
	public Class<?> defineClass(Class<?> originalClass, String className, byte[] classBytes, int off, int len,
			ProtectionDomain protectionDomain) throws ClassFormatError {
		Class<?> result = super.defineClass(originalClass, className, classBytes, off, len, protectionDomain);
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
}
