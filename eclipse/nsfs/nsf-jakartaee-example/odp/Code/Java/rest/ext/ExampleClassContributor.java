package rest.ext;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.openntf.xsp.jaxrs.JAXRSClassContributor;

import jakarta.mvc.security.Csrf;

public class ExampleClassContributor implements JAXRSClassContributor {

	@Override
	public Collection<Class<?>> getClasses() {
		return null;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		return Collections.singletonMap(Csrf.CSRF_PROTECTION, Csrf.CsrfOptions.EXPLICIT);
	}

}
