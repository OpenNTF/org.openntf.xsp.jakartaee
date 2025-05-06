package org.openntf.xsp.jakartaee.module.jakartansf.util;

import java.security.Principal;

import com.ibm.domino.napi.NException;
import com.ibm.domino.napi.c.BackendBridge;
import com.ibm.domino.napi.c.NotesUtil;
import com.ibm.domino.napi.c.xsp.XSPNative;

import org.openntf.xsp.jakartaee.module.jakartansf.NSFJakartaModule;

import jakarta.servlet.http.HttpServletRequest;
import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;

public record ActiveRequestCloner(NSFJakartaModule module, HttpServletRequest request) {
	public ActiveRequest cloneRequest() {
		try {
			Principal principal = request.getUserPrincipal();
			String name = principal == null ? "Anonymous" : principal.getName(); //$NON-NLS-1$
			Session session = XSPNative.createXPageSession(name, 0, false, true);
			BackendBridge.setNoRecycle(session, session, true);
			Database database = session.getDatabase("", module.getMapping().nsfPath()); //$NON-NLS-1$
			BackendBridge.setNoRecycle(session, database, true);
			
			XSPNative.setContextDatabase(session, XSPNative.getDBHandle(database));
			
			String signerName = module.getXspSigner();
			long hSigner = NotesUtil.createUserNameList(signerName);
			Session sessionAsSigner = XSPNative.createXPageSessionExt(signerName, hSigner, false, false, false);
			BackendBridge.setNoRecycle(sessionAsSigner, sessionAsSigner, true);
			Session sessAsSignerFullAccess = XSPNative.createXPageSessionExt(signerName, hSigner, false, false, false);
			BackendBridge.setNoRecycle(sessAsSignerFullAccess, sessAsSignerFullAccess, true);
			
			LSXBEHolder lsxbe = new LSXBEHolder(session, database, sessionAsSigner, sessAsSignerFullAccess, hSigner);
			return new ActiveRequest(module, lsxbe, request);
		} catch(NotesException | NException e) {
			throw new RuntimeException("Encountered exception while cloning Notes objects");
		}
	}
}
