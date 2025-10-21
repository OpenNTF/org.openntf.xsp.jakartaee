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
package org.openntf.xsp.jakarta.mvc;

import java.util.ArrayList;
import java.util.List;

import org.openntf.xsp.jakarta.mvc.weaving.MvcWeavingHook;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.weaving.WeavingHook;

/**
 * @since 3.0.0
 */
public class MvcActivator implements BundleActivator {
	private final List<ServiceRegistration<?>> regs = new ArrayList<>();

	@Override
	public void start(final BundleContext context) throws Exception {
		if (!LibraryUtil.isNotes()) {
			regs.add(context.registerService(WeavingHook.class.getName(), new MvcWeavingHook(), null));
		}
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		regs.forEach(ServiceRegistration::unregister);
		regs.clear();
	}

}
