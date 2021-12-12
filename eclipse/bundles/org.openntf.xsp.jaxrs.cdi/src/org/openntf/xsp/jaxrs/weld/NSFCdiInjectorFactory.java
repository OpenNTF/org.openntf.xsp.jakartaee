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
package org.openntf.xsp.jaxrs.weld;

import org.jboss.resteasy.cdi.CdiInjectorFactory;
import org.openntf.xsp.cdi.util.ContainerUtil;

import com.ibm.xsp.application.ApplicationEx;

import jakarta.enterprise.inject.spi.BeanManager;

public class NSFCdiInjectorFactory extends CdiInjectorFactory {
	@Override
	@SuppressWarnings("nls")
	protected BeanManager lookupBeanManager() {
		ApplicationEx application = ApplicationEx.getInstance();
		if(application == null) {
			throw new IllegalStateException("Unable to locate ApplicationEx!");
		}
		return ContainerUtil.getBeanManager(application);
	}
}
