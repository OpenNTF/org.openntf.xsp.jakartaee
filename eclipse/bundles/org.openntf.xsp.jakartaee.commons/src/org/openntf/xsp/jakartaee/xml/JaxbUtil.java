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
package org.openntf.xsp.jakartaee.xml;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

/**
 * Utilities for working with XML Binding in a low-privilege context.
 *
 * @since 2.0.0
 */
public enum JaxbUtil {
	;

	/**
	 * Creates a new {@link JAXBContext} instance for the provided classes.
	 *
	 * <p>This method performs conversion in an {@link AccessController#doPrivileged} block
	 * to avoid permissions issues in an XPages application.</p>
	 *
     * @param classesToBeBound
     *      list of java classes to be recognized by the new {@link JAXBContext}.
     *      Can be empty, in which case a {@link JAXBContext} that only knows about
     *      spec-defined classes will be returned.
     * @return
     *      A new instance of a <tt>JAXBContext</tt>. Always non-null valid object.
     * @throws JAXBException
     *      if an error was encountered while creating the
     *      <tt>JAXBContext</tt>, such as (but not limited to):
     * <ol>
     *  <li>No JAXB implementation was discovered
     *  <li>Classes use JAXB annotations incorrectly
     *  <li>Classes have colliding annotations (i.e., two classes with the same type name)
     *  <li>The JAXB implementation was unable to locate
     *      provider-specific out-of-band information (such as additional
     *      files generated at the development time.)
     * </ol>
     * @see JAXBContext#newInstance(Class...)
	 */
	public static JAXBContext newInstance(final Class<?>... classesToBeBound) throws JAXBException {
		try {
			return AccessController.doPrivileged((PrivilegedExceptionAction<JAXBContext>)() ->
				JAXBContext.newInstance(classesToBeBound)
			);
		} catch (PrivilegedActionException e) {
			Throwable cause = e.getCause();
			if(cause instanceof JAXBException e2) {
				throw e2;
			} else {
				throw new RuntimeException(e);
			}
		}
	}
}
