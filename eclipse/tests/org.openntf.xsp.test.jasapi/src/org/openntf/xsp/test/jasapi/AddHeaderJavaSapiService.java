/**
 * Copyright (c) 2018-2023 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.test.jasapi;

import com.ibm.domino.bridge.http.jasapi.IJavaSapiEnvironment;
import com.ibm.domino.bridge.http.jasapi.IJavaSapiHttpContextAdapter;
import com.ibm.domino.bridge.http.jasapi.JavaSapiService;

public class AddHeaderJavaSapiService extends JavaSapiService {

	public AddHeaderJavaSapiService(IJavaSapiEnvironment env) {
		super(env);
	}

	@Override
	public int authenticate(IJavaSapiHttpContextAdapter context) {
		return HTEXTENSION_EVENT_DECLINED;
	}

	@Override
	public void startRequest(IJavaSapiHttpContextAdapter context) {
		
	}

	@Override
	public void endRequest(IJavaSapiHttpContextAdapter context) {
		
	}

	@Override
	public String getServiceName() {
		return getClass().getSimpleName();
	}

	@Override
	public int processRequest(IJavaSapiHttpContextAdapter context) {
		return HTEXTENSION_EVENT_DECLINED;
	}

	@Override
	public int rawRequest(IJavaSapiHttpContextAdapter context) {
		context.getResponse().setHeader("X-AddHeaderJavaSapiService", "Hello"); //$NON-NLS-1$ //$NON-NLS-2$
		return HTEXTENSION_SUCCESS;
	}

	@Override
	public int rewriteURL(IJavaSapiHttpContextAdapter context) {
		return HTEXTENSION_EVENT_DECLINED;
	}

}
