/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakarta.nosql.communication.driver.lsxbe.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * This subclass of {@link ObjectInputStream} attempts to load
 * classes from the current context class loader.
 * 
 * @author Jesse Gallagher
 * @since 2.6.0
 */
public class LoaderObjectInputStream extends ObjectInputStream {

	public LoaderObjectInputStream(InputStream in) throws IOException {
		super(in);
	}
	
	@Override
	protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
		try {
			return Class.forName(desc.getName(), true, Thread.currentThread().getContextClassLoader());
		} catch(ClassNotFoundException e) {
			return super.resolveClass(desc);	
		}
	}

}
