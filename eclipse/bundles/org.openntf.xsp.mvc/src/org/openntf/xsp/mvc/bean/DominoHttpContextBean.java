/**
 * Copyright © 2018-2022 Jesse Gallagher
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
package org.openntf.xsp.mvc.bean;

import org.openntf.xsp.mvc.jaxrs.MvcJaxrsServiceParticipant;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Provides CDI access to common Servlet artifacts
 * 
 * @author Jesse Gallagher
 * @since 2.1.0
 */
@ApplicationScoped
public class DominoHttpContextBean {
	@Produces
	@RequestScoped
	public HttpServletRequest getServletRequest() {
		return MvcJaxrsServiceParticipant.CURRENT_REQUEST.get();
	}
	
	@Produces
	@RequestScoped
	public HttpServletResponse getServletResponse() {
		return MvcJaxrsServiceParticipant.CURRENT_RESPONSE.get();
	}
	
	@Produces
	@RequestScoped
	public ServletContext getServletContext() {
		return getServletRequest().getServletContext();
	}
}
