/**
 * Copyright © 2018-2022 Contributors to the XPages Jakarta EE Support Project
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
