package org.openntf.xsp.jsonapi;

import java.util.ArrayList;
import java.util.List;

import org.openntf.xsp.jsonapi.weaving.JsonProviderWeavingHook;
import org.openntf.xsp.jsonapi.weaving.JsonbProviderWeavingHook;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.hooks.weaving.WeavingHook;

/**
 * @since 2.9.0
 */
public class JsonActivator implements BundleActivator {
	private final List<ServiceRegistration<?>> regs = new ArrayList<>();

	@Override
	public void start(BundleContext context) throws Exception {
		regs.add(context.registerService(WeavingHook.class.getName(), new JsonProviderWeavingHook(), null));
		regs.add(context.registerService(WeavingHook.class.getName(), new JsonbProviderWeavingHook(), null));
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		regs.forEach(ServiceRegistration::unregister);
		regs.clear();
	}

}
