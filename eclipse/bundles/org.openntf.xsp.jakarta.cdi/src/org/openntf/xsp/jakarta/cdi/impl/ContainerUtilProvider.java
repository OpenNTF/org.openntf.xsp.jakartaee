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
package org.openntf.xsp.jakarta.cdi.impl;

import org.openntf.xsp.jakarta.cdi.ext.CDIContainerUtility;
import org.openntf.xsp.jakarta.cdi.util.ContainerUtil;
import org.osgi.framework.Bundle;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.2.0
 */
public class ContainerUtilProvider implements CDIContainerUtility {
	
	@Override
	public Object getContainer(ComponentModule module) {
		return ContainerUtil.getContainer(module);
	}

	@Override
	public Object getContainer(Bundle bundle) {
		return ContainerUtil.getContainer(bundle);
	}

	@Override
	public String getThreadContextDatabasePath() {
		return ContainerUtil.getThreadContextDatabasePath();
	}
}
