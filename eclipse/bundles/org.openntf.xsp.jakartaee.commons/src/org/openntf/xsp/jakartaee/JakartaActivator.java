package org.openntf.xsp.jakartaee;

import java.util.ArrayList;
import java.util.List;

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
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		regs.forEach(ServiceRegistration::unregister);
		regs.clear();
	}
}
