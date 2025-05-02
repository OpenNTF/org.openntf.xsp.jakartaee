package org.openntf.xsp.jakartaee.module.jakartansf;

import java.util.Collection;
import java.util.Optional;

import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.IServletFactory;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;
import org.openntf.xsp.jakartaee.module.jakartansf.util.ActiveRequest;
import org.openntf.xsp.jakartaee.module.jakartansf.util.LSXBEHolder;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;

import lotus.domino.Database;
import lotus.domino.Session;

public class NSFJakartaModuleLocator implements ComponentModuleLocator {

	@Override
	public boolean isActive() {
		return ActiveRequest.get().isPresent();
	}

	@Override
	public ComponentModule getActiveModule() {
		return ActiveRequest.get()
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
		return ActiveRequest.get()
			.map(ActiveRequest::module)
			.map(NSFJakartaModule::getNotesDatabase);
	}

	@Override
	public Optional<Database> getUserDatabase() {
		return ActiveRequest.get()
			.map(ActiveRequest::lsxbe)
			.map(LSXBEHolder::database);
	}

	@Override
	public Optional<Session> getUserSession() {
		return ActiveRequest.get()
			.map(ActiveRequest::lsxbe)
			.map(LSXBEHolder::session);
	}

	@Override
	public Optional<Session> getSessionAsSigner() {
		return ActiveRequest.get()
			.map(ActiveRequest::lsxbe)
			.map(LSXBEHolder::sessionAsSigner);
	}

	@Override
	public Optional<Session> getSessionAsSignerWithFullAccess() {
		return ActiveRequest.get()
			.map(ActiveRequest::lsxbe)
			.map(LSXBEHolder::sessionAsSignerFullAccess);
	}

	@Override
	public Optional<ServletContext> getServletContext() {
		return ActiveRequest.get()
			.map(ActiveRequest::module)
			.map(module -> {
				javax.servlet.ServletContext oldCtx = module.getServletContext();
				String contextPath = '/' + module.getMapping().path();
				return ServletUtil.oldToNew(contextPath, oldCtx);
			});
	}

	@Override
	public Optional<HttpServletRequest> getServletRequest() {
		return ActiveRequest.get()
			.map(ActiveRequest::request);
	}

	@Override
	public Collection<? extends IServletFactory> getServletFactories() {
		return ActiveRequest.get()
			.map(ActiveRequest::module)
			.map(NSFJakartaModule::getServletFactories)
			.orElse(null);
	}
}
