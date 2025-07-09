package org.openntf.xsp.jakarta.security.jasapi.cdi;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.security.enterprise.identitystore.IdentityStore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ApplicationScoped
public class JavaSapiSecurityContext implements SecurityContext {

	@Override
	public Principal getCallerPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends Principal> Set<T> getPrincipalsByType(Class<T> pType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCallerInRole(String role) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasAccessToWebResource(String resource, String... methods) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public AuthenticationStatus authenticate(HttpServletRequest request, HttpServletResponse response,
			AuthenticationParameters parameters) {
		Instance<IdentityStore> stores = CDI.current().select(IdentityStore.class);
		if(stores.isResolvable()) {
			CredentialValidationResult result = stores.stream()
				.map(store -> store.validate(parameters.getCredential()))
				.filter(res -> res.getStatus() != CredentialValidationResult.Status.NOT_VALIDATED)
				.findFirst()
				.orElse(CredentialValidationResult.NOT_VALIDATED_RESULT);
			switch(result.getStatus()) {
			case VALID:
				return AuthenticationStatus.SUCCESS;
			case INVALID:
				return AuthenticationStatus.SEND_FAILURE;
			case NOT_VALIDATED:
				return AuthenticationStatus.NOT_DONE;
			}
		}
		return AuthenticationStatus.NOT_DONE;
	}

	@Override
	public Set<String> getAllDeclaredCallerRoles() {
		return Collections.emptySet();
	}

}