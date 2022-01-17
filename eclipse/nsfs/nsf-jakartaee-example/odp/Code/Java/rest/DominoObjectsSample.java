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
	@Named("session")
	Session session;
	
	@Inject
	@Named("sessionAsSigner")
	Session sessionAsSigner;
	
	@Inject
	@Named("sessionAsSignerWithFullAccess")
	Session sessionAsSignerWithFullAccess;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> get() {
		Map<String, Object> result = new HashMap<>();
		result.put("database", toString(database));
		result.put("session", toString(session));
		result.put("sessionAsSigner", toString(sessionAsSigner));
		result.put("sessionAsSignerWithFullAccess", toString(sessionAsSignerWithFullAccess));
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
