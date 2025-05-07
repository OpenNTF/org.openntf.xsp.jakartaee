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
package org.openntf.xsp.jakartaee.module;

import java.util.Map;
import java.util.stream.Stream;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.domino.xsp.module.nsf.NSFComponentModule;
import com.ibm.domino.xsp.module.nsf.RuntimeFileSystem.NSFFile;

import org.openntf.xsp.jakartaee.util.ModuleUtil;

public class NSFComponentModuleProcessor implements ComponentModuleProcessor<NSFComponentModule> {

	@Override
	public boolean canProcess(final ComponentModule module) {
		return module instanceof NSFComponentModule;
	}

	@Override
	public Stream<String> getClassNames(final NSFComponentModule module) {
		return module.getRuntimeFileSystem().getAllResources().entrySet().stream()
			.map(Map.Entry::getKey)
			.filter(key -> key.startsWith(ModuleUtil.PREFIX_CLASSES) && key.endsWith(ModuleUtil.SUFFIX_CLASS))
			.map(key -> key.substring(ModuleUtil.PREFIX_CLASSES.length(), key.length()-ModuleUtil.SUFFIX_CLASS.length()))
			.map(key -> key.replace('/', '.'));
	}

	@Override
	public Stream<String> listFiles(final NSFComponentModule module, final String basePath) {
		String path = basePath;
		boolean listAll = StringUtil.isEmpty(basePath);
		if(!listAll && !path.endsWith("/")) { //$NON-NLS-1$
			path += "/"; //$NON-NLS-1$
		}

		return module.getRuntimeFileSystem().getAllResources().entrySet().stream()
			.filter(entry -> entry.getValue() instanceof NSFFile)
			.map(Map.Entry::getKey)
			.filter(key -> listAll || key.startsWith(basePath));
	}

	@Override
	public String getModuleId(final NSFComponentModule module) {
		return module.getDatabasePath();
	}

}
