package org.openntf.xsp.jakartaee.module.nsf;

import java.util.Optional;

import com.hcl.domino.module.nsf.NotesContext;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;

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
		NotesContext ctx = NotesContext.contextThreadLocal.get();
		if(ctx != null) {
			try {
				return Optional.ofNullable(ctx.getNotesDatabase());
			} catch (NotesAPIException e) {
				throw new RuntimeException(e);
			}
		}
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
		return NSFJakartaModuleService.getActiveRequest()
			.map(ActiveRequest::module)
			.map(module -> {
				javax.servlet.ServletContext oldCtx = module.getServletContext();
				String contextPath = '/' + module.getMapping().path();
				return ServletUtil.oldToNew(contextPath, oldCtx);
			});
	}

	@Override
	public Optional<HttpServletRequest> getServletRequest() {
		return NSFJakartaModuleService.getActiveRequest()
			.map(ActiveRequest::request);
	}

}
