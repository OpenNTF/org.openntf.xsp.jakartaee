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
package org.openntf.xsp.test.beanbundle.servlet;


import org.jboss.resteasy.cdi.CdiInjectorFactory;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.ForwardingBeanManager;
import org.openntf.xsp.jakarta.cdi.util.ContainerUtil;
import org.osgi.framework.FrameworkUtil;

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.CDI;

public class ExampleCDIInjectorFactory extends CdiInjectorFactory {
	private static final BeanManager manager = new ForwardingBeanManager() {
		private static final long serialVersionUID = 1L;

		@Override
		public BeanManager delegate() {
			CDI<Object> container = ContainerUtil.getContainer(FrameworkUtil.getBundle(ExampleCDIInjectorFactory.class));
			
			BeanManager manager = container.getBeanManager();
			if(manager instanceof BeanManagerImpl) {
				return manager;
			} else if(manager instanceof ForwardingBeanManager) {
				return ((ForwardingBeanManager)manager).delegate();
			} else {
				throw new IllegalStateException("Cannot find BeanManagerImpl in " + manager); //$NON-NLS-1$
			}
		}
	};

	@Override
	protected BeanManager lookupBeanManager() {
		return manager;
	}

}
