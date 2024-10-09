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
package org.openntf.xsp.microprofile.library;

import com.ibm.xsp.library.AbstractXspLibrary;
import com.ibm.xsp.library.CoreLibrary;

import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.osgi.framework.FrameworkUtil;

public class MicroProfileLibrary extends AbstractXspLibrary {

	@Override
	public String getLibraryId() {
		return LibraryUtil.LIBRARY_MICROPROFILE;
	}

	@Override
	public String getPluginId() {
		return FrameworkUtil.getBundle(getClass()).getSymbolicName();
	}

	@Override
	public String[] getDependencies() {
		return new String[] {
			CoreLibrary.LIBRARY_ID,
			LibraryUtil.LIBRARY_CORE
		};
	}

	@Override
	public boolean isGlobalScope() {
		return false;
	}

}
