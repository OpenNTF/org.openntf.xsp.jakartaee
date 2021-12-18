/**
 * Copyright © 2018-2021 Jesse Gallagher
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
package org.openntf.xsp.cdi.impl;

import java.io.IOException;

import org.openntf.xsp.cdi.ext.CDIContainerUtility;
import org.openntf.xsp.cdi.util.ContainerUtil;
import org.osgi.framework.Bundle;

import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.xsp.application.ApplicationEx;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.2.0
 */
public class ContainerUtilProvider implements CDIContainerUtility {

	@Override
	public Object getContainer(NotesDatabase database) throws NotesAPIException, IOException {
		return ContainerUtil.getContainer(database);
	}

	@Override
	public Object getContainer(ApplicationEx app) {
		return ContainerUtil.getContainer(app);
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
