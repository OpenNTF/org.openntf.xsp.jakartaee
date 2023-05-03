package org.openntf.xsp.jakartaee.bridge.jasapi.init;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openntf.xsp.jakartaee.bridge.jasapi.JasapiServiceFactory;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import com.ibm.designer.runtime.domino.adapter.HttpService;
import com.ibm.designer.runtime.domino.adapter.IServiceFactory;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;
import com.ibm.domino.bridge.http.jasapi.JavaSapiEnvironment;
import com.ibm.domino.bridge.http.jasapi.JavaSapiService;

/**
 * This implementation of {@link IServiceFactory} doesn't provide
 * HTTP services, but is instead used to initialize the custom JavaSapi
 * extension point early in the HTTP lifecycle.
 * 
 * @author Jesse Gallagher
 * @since 2.12.0
 */
public class JasapiEarlyInitFactory implements IServiceFactory {
	private static final Logger log = Logger.getLogger(JasapiEarlyInitFactory.class.getPackage().getName());

	@Override
	public HttpService[] getServices(LCDEnvironment arg0) {
		AccessController.doPrivileged((PrivilegedAction<Void>)() -> {
			try {
				JavaSapiEnvironment env = findEnvironment();
				env.registerServices();
				
				List<JasapiServiceFactory> extensions = LibraryUtil.findExtensions(JasapiServiceFactory.class);
				if(!extensions.isEmpty()) {
					Field servicesField = JavaSapiEnvironment.class.getDeclaredField("services"); //$NON-NLS-1$
					servicesField.setAccessible(true);
					
					JavaSapiService[] existing = (JavaSapiService[])servicesField.get(env);
					List<JavaSapiService> services = new ArrayList<>(Arrays.asList(existing));
					extensions.stream()
						.map(fac -> fac.getServices(env))
						.filter(Objects::nonNull)
						.flatMap(Collection::stream)
						.filter(Objects::nonNull)
						.forEach(services::add);
					servicesField.set(env, services.toArray(new JavaSapiService[services.size()]));
				}
				
			} catch(Throwable t) {
				if(log.isLoggable(Level.SEVERE)) {
					log.log(Level.SEVERE, "Encountered exception initializing JavaSapi services", t);
				}
			}
			
			return null;
		});
		return new HttpService[0];
	}

	private JavaSapiEnvironment findEnvironment() {
		JavaSapiEnvironment env = JavaSapiEnvironment.getInstance();
		if(env == null) {
			env = new JavaSapiEnvironment();
		}
		return env;
	}
}
