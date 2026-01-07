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
package org.openntf.xsp.jakarta.cdi.bean;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Provides HTTP contextual objects when available.
 *
 * @since 2.16.0
 */
@RequestScoped
public class HttpContextBean {
	static ThreadLocal<HttpServletResponse> THREAD_RESPONSES = new ThreadLocal<>();

	public static void setThreadResponse(final HttpServletResponse response) {
		THREAD_RESPONSES.set(response);
	}

	// Oddly, starting with version 3.0 (JEE 10), just returning the request
	//   and response directly leads to MVC re-using the same object across
	//   multiple requests. Use these constantly-proxying objects instead to
	//   avoid the trouble.
	// TODO figure out why this happens, when it didn't in the JEE 9 versions

	@Produces
	public HttpServletRequest getServletRequest() {
		return ProxyingHttpServletRequest.INSTANCE;
	}

	@Produces
	public HttpServletResponse getServletResponse() {
		return ProxyingHttpServletResponse.INSTANCE;
	}

	@Produces
	public ServletContext getServletContext() {
		return getServletRequest().getServletContext();
	}
}
