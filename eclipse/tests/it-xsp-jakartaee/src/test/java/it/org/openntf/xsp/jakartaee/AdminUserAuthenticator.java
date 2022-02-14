package it.org.openntf.xsp.jakartaee;

import java.io.IOException;
import java.util.Base64;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

public class AdminUserAuthenticator implements ClientRequestFilter {
	public static final String USER = "Jakarta EE Test"; //$NON-NLS-1$
	public static final String PASSWORD = "ThisIsATestPassword"; //$NON-NLS-1$
	
	@Override
	public void filter(ClientRequestContext requestContext) throws IOException {
		String b64 = Base64.getEncoder().encodeToString((USER + ':' + PASSWORD).getBytes());
		requestContext.getHeaders().add("Authorization", "Basic " + b64); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
