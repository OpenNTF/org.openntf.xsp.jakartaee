/**
 * Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	protected HttpURLConnection createConnection(final ClientInvocation request) throws IOException {
		HttpURLConnection conn = super.createConnection(request);
		if(conn instanceof HttpsURLConnection sslConn) {
			sslConn.setSSLSocketFactory(new PatchingDominoSSLSocketFactory());
		}
		return conn;
	}
}
