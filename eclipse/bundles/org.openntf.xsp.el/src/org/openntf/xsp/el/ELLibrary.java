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
package org.openntf.xsp.el;

import com.ibm.xsp.library.AbstractXspLibrary;

public class ELLibrary extends AbstractXspLibrary {

	public static final String LIBRARY_ID = "org.openntf.xsp.el"; //$NON-NLS-1$
	public static final String PLUGIN_ID = LIBRARY_ID;
	public static final String PROP_PREFIX =  "org.openntf.xsp.el.prefix"; //$NON-NLS-1$
	
	@Override
	public String getLibraryId() {
		return LIBRARY_ID;
	}
	
	@Override
	public String getPluginId() {
		return PLUGIN_ID;
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
