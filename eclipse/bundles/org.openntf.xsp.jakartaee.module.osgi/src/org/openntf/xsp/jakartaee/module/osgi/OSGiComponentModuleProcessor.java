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
package org.openntf.xsp.jakartaee.module.osgi;

import java.util.stream.Stream;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.domino.xsp.adapter.osgi.AbstractOSGIModule;

import org.openntf.xsp.jakartaee.module.ComponentModuleProcessor;

/**
 * @since 3.4.0
 */
public class OSGiComponentModuleProcessor implements ComponentModuleProcessor<AbstractOSGIModule> {

	@Override
	public boolean canProcess(ComponentModule module) {
		return module instanceof AbstractOSGIModule;
	}

	@Override
	public Stream<String> getClassNames(AbstractOSGIModule module) {
		return Stream.empty();
	}

	@Override
	public Stream<String> listFiles(AbstractOSGIModule module, String basePath) {
		return Stream.empty();
	}
	
	@Override
	public boolean usesBundleClassLoader(AbstractOSGIModule module) {
		return true;
	}
	
	@Override
	public boolean hasImplicitCdi(AbstractOSGIModule module) {
		return true;
	}

}
