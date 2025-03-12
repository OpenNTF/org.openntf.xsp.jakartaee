package org.openntf.xsp.jakarta.rest.client;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import com.ibm.domino.napi.ssl.DominoSSLSocketFactory;

/**
 * This subclass of {@link DominoSSLSocketFactory} replaces the {@link #createSSLContext()}
 * method with a variant that doesn't create an LSXBE session, avoiding a
 * "Cannot create a session from an agent" message in the logs.
 * 
 * @since 3.4.0
 * @see <a href="https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/616">Issue #616</a>
 */
public class PatchingDominoSSLSocketFactory extends DominoSSLSocketFactory {
	@Override
	protected SSLContext createSSLContext() {
		try {
			SSLContext sslContext = SSLContext.getInstance("TLSv1.2"); //$NON-NLS-1$
			sslContext.init(null, new TrustManager[] { createDominoX509TrustManager() }, null);
			return sslContext;
		} catch (NoSuchAlgorithmException | KeyManagementException e) {
			throw new RuntimeException(e);
		}
	}
}
