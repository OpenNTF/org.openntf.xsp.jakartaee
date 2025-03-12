package org.openntf.xsp.jakarta.rest.client;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;

import org.jboss.resteasy.client.jaxrs.engines.URLConnectionEngine;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocation;

/**
 * This subclass of {@link URLConnectionEngine} modifies the {@link #createConnection}
 * method to apply a {@link PatchingDominoSSLSocketFactory} instance to HTTPS connections
 * to avoid a "Cannot create a session from an agent" message in the logs.
 * 
 * @since 3.4.0
 * @see <a href="https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/616">Issue #616</a>
 */
public class PatchingURLConnectionEngine extends URLConnectionEngine {
	@Override
	protected HttpURLConnection createConnection(ClientInvocation request) throws IOException {
		HttpURLConnection conn = super.createConnection(request);
		if(conn instanceof HttpsURLConnection sslConn) {
			sslConn.setSSLSocketFactory(new PatchingDominoSSLSocketFactory());
		}
		return conn;
	}
}
