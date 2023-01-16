/**
 * Copyright (c) 2018-2023 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakartaee;

import java.util.ArrayList;
import java.util.List;

import org.openntf.xsp.jakartaee.weaving.MailWeavingHook;
import org.openntf.xsp.jakartaee.weaving.UtilWeavingHook;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.weaving.WeavingHook;

public class JakartaActivator implements BundleActivator {
	private final List<ServiceRegistration<?>> regs = new ArrayList<>();

	@Override
	public void start(BundleContext context) throws Exception {
		regs.add(context.registerService(WeavingHook.class.getName(), new UtilWeavingHook(), null));
		regs.add(context.registerService(WeavingHook.class.getName(), new MailWeavingHook(), null));
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		regs.forEach(ServiceRegistration::unregister);
		regs.clear();
	}
}
