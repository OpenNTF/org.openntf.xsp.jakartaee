package org.openntf.xsp.jakarta.bridge.jasapi.init;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.openntf.xsp.jakarta.bridge.jasapi.JasapiServiceFactory;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import com.ibm.designer.runtime.domino.adapter.HttpService;
import com.ibm.designer.runtime.domino.adapter.IServiceFactory;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;
import com.ibm.domino.bridge.http.jasapi.JavaSapiEnvironment;
import com.ibm.domino.bridge.http.jasapi.JavaSapiService;

public class JasapiEarlyInitFactory implements IServiceFactory {

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
						.forEach(services::addAll);
					servicesField.set(env, services.toArray(new JavaSapiService[services.size()]));
				}
				
			} catch(Throwable t) {
				t.printStackTrace();
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
