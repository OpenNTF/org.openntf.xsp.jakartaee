/**
 * Copyright © 2018-2022 Martin Pradny and Jesse Gallagher
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
package org.openntf.xsp.jaxrs.security;

import java.io.IOException;
import java.lang.reflect.Method;

import jakarta.annotation.Priority;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

/**
 * Checks incoming requests for security annotations and enforces them using
 * the current security context.
 * 
 * @author Jesse Gallagher
 * @since 2.2.0
 */
@Provider
@Priority(Priorities.AUTHORIZATION-1)
public class RolesAllowedFilter implements ContainerRequestFilter {
	@Context
	ResourceInfo resourceInfo;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		if(!isAllowed(requestContext)) {
			ResponseBuilder response = Response.status(Response.Status.UNAUTHORIZED);
			requestContext.abortWith(response.build());
		}
	}
	
	private boolean isAllowed(ContainerRequestContext requestContext) {
		Method method = resourceInfo.getResourceMethod();
		Class<?> clazz = resourceInfo.getResourceClass();
		
		if(method.isAnnotationPresent(PermitAll.class)) {
			return true;
		}
		if(method.isAnnotationPresent(DenyAll.class)) {
			return false;
		}
		
		RolesAllowed roles = method.getAnnotation(RolesAllowed.class);
		if(roles == null) {
			roles = clazz.getAnnotation(RolesAllowed.class);
		}
		if(roles != null) {
			SecurityContext sec = requestContext.getSecurityContext();
			for(String role : roles.value()) {
				if(sec.isUserInRole(role)) {
					return true;
				}
			}
			return false;
		}
		
		return true;
	}

}