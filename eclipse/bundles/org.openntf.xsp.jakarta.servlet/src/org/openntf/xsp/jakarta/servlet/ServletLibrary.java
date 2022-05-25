/**
 * Copyright © 2018-2022 Jesse Gallagher
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
package org.openntf.xsp.jakarta.servlet;

import org.osgi.framework.FrameworkUtil;

import com.ibm.xsp.library.AbstractXspLibrary;

import jakarta.servlet.annotation.WebServlet;

/**
 * {@link com.ibm.xsp.library.XspLibrary XspLibrary} that denotes that an NSF should
 * opt in to {@link WebServlet}-annotated Servlet processing.
 * 
 * @author Jesse Gallagher
 * @since 2.5.0
 */
public class ServletLibrary extends AbstractXspLibrary {

	public static final String LIBRARY_ID = ServletLibrary.class.getPackage().getName();

	@Override
	public String getLibraryId() {
		return LIBRARY_ID;
	}
	
	@Override
	public String getPluginId() {
		return FrameworkUtil.getBundle(getClass()).getSymbolicName();
	}
	
	@Override
	public String[] getDependencies() {
		return new String[] {
			"com.ibm.xsp.core.library", //$NON-NLS-1$
			"com.ibm.xsp.extsn.library", //$NON-NLS-1$
			"com.ibm.xsp.designer.library" //$NON-NLS-1$
		};
	}
	
	@Override
	public boolean isGlobalScope() {
		return false;
	}

}
