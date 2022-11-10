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
package org.openntf.xsp.cdi.discovery;

import java.util.Collection;
import java.util.stream.Collectors;

import org.openntf.xsp.jakartaee.util.ModuleUtil;

import com.ibm.domino.xsp.module.nsf.NotesContext;

/**
 * This class is responsible for locating and loading bean classes from the
 * context NSF when active.
 * 
 * <p>Originally, this work was done by {@link NSFBeanArchiveHandler}, but
 * this mechanism avoids the trouble of handing off just string class names.</p>
 * 
 * @author Jesse Gallagher
 * @since 2.9.0
 */
public class NSFComponentModuleClassContributor implements WeldBeanClassContributor {

	@Override
	public Collection<Class<?>> getBeanClasses() {
		NotesContext notesContext = NotesContext.getCurrentUnchecked();
		if(notesContext != null) {
			return ModuleUtil.getClasses(notesContext.getModule())
				.collect(Collectors.toSet());
		}
		return null;
	}

}
