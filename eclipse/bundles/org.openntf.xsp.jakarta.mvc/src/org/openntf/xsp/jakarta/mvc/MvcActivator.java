package org.openntf.xsp.jakarta.mvc;

import java.util.ArrayList;
import java.util.List;

import org.openntf.xsp.jakarta.mvc.weaving.MvcWeavingHook;
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
	public void start(BundleContext context) throws Exception {
		 regs.add(context.registerService(WeavingHook.class.getName(), new MvcWeavingHook(), null));
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		regs.forEach(ServiceRegistration::unregister);
		regs.clear();
	}

}
