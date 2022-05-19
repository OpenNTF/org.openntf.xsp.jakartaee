package org.openntf.xsp.jakarta.bridge.jasapi;

import java.util.Collection;

import com.ibm.domino.bridge.http.jasapi.IJavaSapiEnvironment;
import com.ibm.domino.bridge.http.jasapi.JavaSapiService;

public interface JasapiServiceFactory {
	public static final String EXTENSION_ID = JasapiServiceFactory.class.getName();

	Collection<JavaSapiService> getServices(IJavaSapiEnvironment env);

}
