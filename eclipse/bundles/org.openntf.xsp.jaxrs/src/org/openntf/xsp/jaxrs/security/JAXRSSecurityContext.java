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
import java.util.HashSet;
import java.util.List;

import com.ibm.xsp.extlib.util.ExtLibUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.SecurityContext;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;

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
			this.roles = new HashSet<>();
			// TODO see if this can be done more simply in the NAPI
			Database database = ExtLibUtil.getCurrentDatabase();
			try {
				Session session = database.getParent();
				Document doc = database.createDocument();
				try {
					// session.getUserNameList returns the name of the server
					@SuppressWarnings("unchecked")
					List<String> names = session.evaluate(" @UserNamesList ", doc); //$NON-NLS-1$
					roles.addAll(names);
				} finally {
					doc.recycle();
				}
			} catch(NotesException e) {
				throw new RuntimeException(e);
			}
		}
		return this.roles;
	}

}
