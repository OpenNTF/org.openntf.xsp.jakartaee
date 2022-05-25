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
package org.openntf.xsp.jaxrs.weld;

import org.jboss.resteasy.cdi.CdiInjectorFactory;
import org.openntf.xsp.cdi.util.ContainerUtil;

import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.domino.xsp.module.nsf.NotesContext;
import com.ibm.xsp.application.ApplicationEx;

import jakarta.enterprise.inject.spi.BeanManager;

public class NSFCdiInjectorFactory extends CdiInjectorFactory {
	@Override
	@SuppressWarnings("nls")
	protected BeanManager lookupBeanManager() {
		ApplicationEx application = ApplicationEx.getInstance();
		if(application != null) {
			return ContainerUtil.getBeanManager(application);
		}
		NotesContext ctx = NotesContext.getCurrentUnchecked();
		if(ctx != null) {
			try {
			NotesDatabase database = ctx.getNotesDatabase();
				if(database != null) {
					return ContainerUtil.getBeanManager(database);
				}
			} catch(NotesAPIException e) {
				throw new RuntimeException(e);
			}
		}
		throw new IllegalStateException("Unable to locate active application!");
	}
}
