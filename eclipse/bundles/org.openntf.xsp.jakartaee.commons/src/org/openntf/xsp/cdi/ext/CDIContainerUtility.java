/**
 * Copyright © 2018-2022 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.cdi.ext;

import java.io.IOException;

import org.osgi.framework.Bundle;

import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.xsp.application.ApplicationEx;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.2.0
 */
public interface CDIContainerUtility {
	Object getContainer(NotesDatabase database) throws NotesAPIException, IOException;
	Object getContainer(ApplicationEx app);
	Object getContainer(Bundle bundle);
	String getThreadContextDatabasePath();
}
