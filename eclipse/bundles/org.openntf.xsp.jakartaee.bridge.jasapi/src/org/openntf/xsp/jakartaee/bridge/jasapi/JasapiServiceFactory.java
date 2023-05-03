package org.openntf.xsp.jakartaee.bridge.jasapi;

import java.util.Collection;

import com.ibm.domino.bridge.http.jasapi.IJavaSapiEnvironment;
import com.ibm.domino.bridge.http.jasapi.JavaSapiService;

/**
 * This extention point interface allows for plugins to register JavaSapi
 * services that will be loaded at HTTP init.
 * 
 * @author Jesse Gallagher
 * @since 2.12.0
 */
public interface JasapiServiceFactory {
	public static final String EXTENSION_ID = JasapiServiceFactory.class.getName();

	Collection<JavaSapiService> getServices(IJavaSapiEnvironment env);

}
