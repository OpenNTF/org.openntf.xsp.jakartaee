package security;

import org.openntf.xsp.jakartaee.jasapi.JavaSapiContext;
import org.openntf.xsp.jakartaee.jasapi.JavaSapiExtension;

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.security.enterprise.AuthenticationException;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class SecurityJavaSapiExtension implements JavaSapiExtension {
	@Override
	public Result authenticate(JavaSapiContext context) {
		Instance<HttpAuthenticationMechanism> mechanisms = CDI.current().select(HttpAuthenticationMechanism.class);
		if(mechanisms.isResolvable()) {
			HttpServletRequest request = new JavaSapiHttpServletRequest(context.getRequest());
			HttpServletResponse response = new JavaSapiHttpServletResponse(context.getResponse());
			HttpMessageContext httpContext = new JavaSapiMessageContext(context);
			
			AuthenticationStatus status = mechanisms.stream()
				.map(mechanism -> {
					try {
						return mechanism.validateRequest(request, response, httpContext);
					} catch (AuthenticationException e) {
						throw new RuntimeException(e);
					}
				})
				.filter(s -> s != AuthenticationStatus.NOT_DONE)
				.findFirst()
				.orElse(AuthenticationStatus.NOT_DONE);
			if(status != AuthenticationStatus.NOT_DONE) {
				// TODO map to other statuses/actions
				System.out.println("going to signal authenticated");
				return Result.REQUEST_AUTHENTICATED;
			}
		}
		return JavaSapiExtension.super.authenticate(context);
	}
}
