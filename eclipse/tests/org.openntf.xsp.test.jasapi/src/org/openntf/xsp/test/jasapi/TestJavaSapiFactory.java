package org.openntf.xsp.test.jasapi;

import java.util.Collection;
import java.util.Collections;

import org.openntf.xsp.jakartaee.bridge.jasapi.JasapiServiceFactory;

import com.ibm.domino.bridge.http.jasapi.IJavaSapiEnvironment;
import com.ibm.domino.bridge.http.jasapi.JavaSapiService;

public class TestJavaSapiFactory implements JasapiServiceFactory {

	@Override
	public Collection<JavaSapiService> getServices(IJavaSapiEnvironment env) {
		return Collections.singleton(new AddHeaderJavaSapiService(env));
	}

}
