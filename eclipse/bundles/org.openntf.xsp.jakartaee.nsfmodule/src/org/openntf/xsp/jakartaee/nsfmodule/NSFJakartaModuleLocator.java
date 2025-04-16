package org.openntf.xsp.jakartaee.nsfmodule;

import java.util.Optional;

import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;

import lotus.domino.Database;
import lotus.domino.Session;

public class NSFJakartaModuleLocator implements ComponentModuleLocator {

	@Override
	public boolean isActive() {
		return NSFJakartaModuleService.getActiveRequest().isPresent();
	}

	@Override
	public ComponentModule getActiveModule() {
		return NSFJakartaModuleService.getActiveRequest()
			.map(ActiveRequest::module)
			.orElse(null);
	}

	@Override
	public Optional<String> getVersion() {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<NotesDatabase> getNotesDatabase() {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<Database> getUserDatabase() {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<Session> getUserSession() {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<Session> getSessionAsSigner() {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<Session> getSessionAsSignerWithFullAccess() {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<ServletContext> getServletContext() {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public Optional<HttpServletRequest> getServletRequest() {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

}
