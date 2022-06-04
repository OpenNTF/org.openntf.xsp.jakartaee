package org.openntf.xsp.jakarta.bridge.jasapi.test;

import java.util.Arrays;
import java.util.Collection;

import org.openntf.xsp.jakarta.bridge.jasapi.JasapiServiceFactory;

import com.ibm.domino.bridge.http.jasapi.IJavaSapiEnvironment;
import com.ibm.domino.bridge.http.jasapi.JavaSapiService;

public class JakartaSapiServiceFactory implements JasapiServiceFactory {

	@Override
	public Collection<JavaSapiService> getServices(IJavaSapiEnvironment env) {
		return Arrays.asList(
			new JwtService(env)
		);
	}

}
