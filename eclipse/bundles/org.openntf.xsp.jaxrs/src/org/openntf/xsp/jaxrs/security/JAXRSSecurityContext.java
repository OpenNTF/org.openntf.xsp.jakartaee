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

import java.security.Principal;
import java.util.Collection;

import org.openntf.xsp.jakartaee.util.LibraryUtil;

import com.ibm.xsp.extlib.util.ExtLibUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.SecurityContext;
import lotus.domino.Database;
import lotus.domino.NotesException;

public class JAXRSSecurityContext implements SecurityContext {
	private final HttpServletRequest req;
	private Collection<String> roles;

	public JAXRSSecurityContext(HttpServletRequest req) {
		this.req = req;
	}

	@Override
	public Principal getUserPrincipal() {
		return req.getUserPrincipal();
	}

	@Override
	public boolean isUserInRole(final String role) {
		if(role == null) {
			return false;
		}
		switch(role) {
		case "login": //$NON-NLS-1$
			return !"Anonymous".equals(req.getUserPrincipal().getName()); //$NON-NLS-1$
		default:
			return getRoles().contains(role);
		}
	}

	@Override
	public boolean isSecure() {
		return req.isSecure();
	}

	@Override
	public String getAuthenticationScheme() {
		// TODO look this up from the active authentication filter
		return FORM_AUTH;
	}
	
	private Collection<String> getRoles() {
		if(this.roles == null) {
			Database database = ExtLibUtil.getCurrentDatabase();
			try {
				this.roles = LibraryUtil.getUserNamesList(database);
			} catch(NotesException e) {
				throw new RuntimeException(e);
			}
		}
		return this.roles;
	}

}
