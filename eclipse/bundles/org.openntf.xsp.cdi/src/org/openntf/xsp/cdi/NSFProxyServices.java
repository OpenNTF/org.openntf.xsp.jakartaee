/**
 * Copyright © 2018-2021 Jesse Gallagher
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
package org.openntf.xsp.cdi;

import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

import org.jboss.weld.bean.proxy.util.WeldDefaultProxyServices;

/**
 * This subclass of {@link WeldDefaultProxyServices} keeps an internal cache of generated
 * classes. This is to avoid {@code java.lang.LinkageError: A duplicate class definition for jakarta/enterprise/event/Event$WeldEvent$Proxy$_$$_Weld$Proxy$ is found}
 * 
 * @author Jesse Gallagher
 * @since 2.0.0
 */
public class NSFProxyServices extends WeldDefaultProxyServices {
	private Map<String, Class<?>> classCache = new HashMap<>();
	
	@Override
	public Class<?> defineClass(Class<?> originalClass, String className, byte[] classBytes, int off, int len)
			throws ClassFormatError {
		Class<?> result = super.defineClass(originalClass, className, classBytes, off, len);
		if(result != null) {
			classCache.put(className, result);
		}
		return result;
	}
	
	@Override
	public Class<?> defineClass(Class<?> originalClass, String className, byte[] classBytes, int off, int len,
			ProtectionDomain protectionDomain) throws ClassFormatError {
		Class<?> result = super.defineClass(originalClass, className, classBytes, off, len, protectionDomain);
		if(result != null) {
			classCache.put(className, result);
		}
		return result;
	}
	
	@Override
	public synchronized Class<?> loadClass(Class<?> originalClass, String classBinaryName) throws ClassNotFoundException {
		if(classCache.containsKey(classBinaryName)) {
			return classCache.get(classBinaryName);
		}
		return super.loadClass(originalClass, classBinaryName);
	}
}
