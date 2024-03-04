package security;

import java.util.Collections;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.security.enterprise.AuthenticationException;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.SecurityContext;
import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ApplicationScoped
public class ExampleAuthenticationMechanism implements HttpAuthenticationMechanism {
	
	@Inject
	private SecurityContext securityContext;
	
	@Override
	public AuthenticationStatus validateRequest(HttpServletRequest request, HttpServletResponse response,
			HttpMessageContext httpMessageContext) throws AuthenticationException {
		ExampleIdentityStore.HeaderCredential cred = new ExampleIdentityStore.HeaderCredential(request.getHeader("X-MySpecialHeader"));
		AuthenticationStatus result = securityContext.authenticate(request, response, AuthenticationParameters.withParams().credential(cred));
		if(result == AuthenticationStatus.SUCCESS) {
			return httpMessageContext.notifyContainerAboutLogin(cred.getName(), Collections.emptySet());
		} else {
			return httpMessageContext.doNothing();
		}
		
	}

}
