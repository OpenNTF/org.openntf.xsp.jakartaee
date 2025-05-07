/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakartaee.module.jakartansf;

import java.util.stream.Stream;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;

import org.openntf.xsp.jakartaee.module.ComponentModuleProcessor;

/**
 * @since 3.4.0
 */
public class NSFJakartaModuleProcessor implements ComponentModuleProcessor<NSFJakartaModule> {

	@Override
	public boolean canProcess(ComponentModule module) {
		return module instanceof NSFJakartaModule;
	}

	@Override
	public Stream<String> getClassNames(NSFJakartaModule module) {
		return module.getModuleClassLoader().getClassNames().stream();
	}

	@Override
	public Stream<String> listFiles(NSFJakartaModule module, String basePath) {
		return module.listFiles(basePath);
	}
	
	@Override
	public String getModuleId(NSFJakartaModule module) {
		return module.getClass().getSimpleName() + '-' + module.getMapping().nsfPath();
	}
	
	@Override
	public boolean emulateServletEvents(NSFJakartaModule module) {
		return false;
	}

}
