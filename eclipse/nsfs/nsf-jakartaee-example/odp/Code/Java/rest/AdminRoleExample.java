/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
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
package rest;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.SecurityContext;

@Path("adminrole")
public class AdminRoleExample {
	
	@Inject
	private SecurityContext securityContext;
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed("[Admin]")
	public String get() {
		return "I think you're an admin, " + securityContext.getUserPrincipal().getName();
	}
	
	@Path("invaliduser")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed("dfsesdf fd fsdf sdf sdfsddfsdfsdfsdfsd ds d sdf")
	public String getFakeUser() {
		return "It's unlikely, but I suppose possible, that you're allowed to see this.";
	}
	
	@Path("login")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed("login")
	public String getLoginRole() {
		return "I think you're an authenticated user";
	}
}