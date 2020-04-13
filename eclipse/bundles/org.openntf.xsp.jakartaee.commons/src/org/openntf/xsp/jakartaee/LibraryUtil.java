/**
 * Copyright © 2018-2020 Jesse Gallagher
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
package org.openntf.xsp.jakartaee;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.xsp.application.ApplicationEx;

/**
 * Utility methods for working with XSP Libraries.
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public enum LibraryUtil {
	;
	
	/**
	 * Determines whether the provided {@link ApplicationEx} uses the provided library
	 * ID.
	 * 
	 * @param libraryId the library ID to look for
	 * @param app the application instance to check
	 * @return whether the library is loaded by the application
	 */
	public static boolean usesLibrary(String libraryId, ApplicationEx app) {
		String prop = app.getProperty("xsp.library.depends", ""); //$NON-NLS-1$ //$NON-NLS-2$
		return Arrays.asList(prop.split(",")).contains(libraryId); //$NON-NLS-1$
	}

	/**
	 * Determines whether the provided {@link ComponentModule} uses the provided library
	 * ID.
	 * 
	 * @param libraryId the library ID to look for
	 * @param module the component module to check
	 * @return whether the library is loaded by the application
	 * @since 1.2.0
	 * @throws IOException if there is a problem reading the xsp.properties file in the module
	 */
	public static boolean usesLibrary(String libraryId, ComponentModule module) throws IOException {
		Properties props = new Properties();
		try(InputStream is = module.getResourceAsStream("/WEB-INF/xsp.properties")) { //$NON-NLS-1$
			props.load(is);
		}
		String prop = props.getProperty("xsp.library.depends", ""); //$NON-NLS-1$ //$NON-NLS-2$
		return Arrays.asList(prop.split(",")).contains(libraryId); //$NON-NLS-1$
	}
}
