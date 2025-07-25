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

import java.util.HashMap;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lotus.domino.Database;
import lotus.domino.Session;

@Path("dominoObjects")
public class DominoObjectsSample {
	@Inject
	Database database;
	
	@Inject
	@Named("dominoSession")
	Session session;
	
	@Inject
	@Named("dominoSessionAsSigner")
	Session sessionAsSigner;
	
	@Inject
	@Named("dominoSessionAsSignerWithFullAccess")
	Session sessionAsSignerWithFullAccess;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> get() {
		Map<String, Object> result = new HashMap<>();
		result.put("database", toString(database));
		result.put("dominoSession", toString(session));
		result.put("dominoSessionAsSigner", toString(sessionAsSigner));
		result.put("dominoSessionAsSignerWithFullAccess", toString(sessionAsSignerWithFullAccess));
		return result;
	}
	
	private String toString(Object obj) {
		if(obj == null) {
			return "null";
		} else {
			return obj.getClass().getName() + "@" + Integer.toHexString(obj.hashCode());
		}
	}
}
