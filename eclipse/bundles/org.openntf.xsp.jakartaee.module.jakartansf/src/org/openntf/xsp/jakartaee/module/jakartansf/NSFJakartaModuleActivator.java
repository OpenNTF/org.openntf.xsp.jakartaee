package org.openntf.xsp.jakartaee.module.jakartansf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import org.openntf.xsp.jakartaee.module.jakartansf.io.NSFJakartaFileSystem;
import org.openntf.xsp.jakartaee.module.jakartansf.io.NSFJakartaURLStreamHandlerService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.url.URLConstants;
import org.osgi.service.url.URLStreamHandlerService;

public class NSFJakartaModuleActivator implements BundleActivator {
	private final Collection<ServiceRegistration<?>> registrations = new ArrayList<>();
	
	@Override
	public void start(BundleContext context) throws Exception {
		Hashtable<String, Object> urlProps = new Hashtable<>();
		urlProps.put(URLConstants.URL_HANDLER_PROTOCOL, new String[] { NSFJakartaFileSystem.URLSCHEME });
		
		registrations.add(context.registerService(URLStreamHandlerService.class, new NSFJakartaURLStreamHandlerService(), urlProps));
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		registrations.forEach(ServiceRegistration::unregister);
	}

}
