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
package rest;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("adminrole")
public class AdminRoleExample {
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed("[Admin]")
	public String get() {
		return "I think you're an admin!";
	}
	
	@Path("invaliduser")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@RolesAllowed("dfsesdf fd fsdf sdf sdfsddfsdfsdfsdfsd ds d sdf")
	public String getFakeUser() {
		return "It's unlikely, but I suppose possible, that you're allowed to see this.";
	}
}