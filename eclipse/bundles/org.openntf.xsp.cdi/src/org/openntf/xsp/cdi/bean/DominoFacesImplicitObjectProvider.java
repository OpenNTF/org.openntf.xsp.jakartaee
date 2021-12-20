package org.openntf.xsp.cdi.bean;

import javax.faces.context.FacesContext;

import com.ibm.domino.xsp.module.nsf.NotesContext;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import lotus.domino.Database;
import lotus.domino.Session;

/**
 * This bean provides access to implicit objects from the current
 * {@link FacesContext}, when available.
 * 
 * @author Jesse Gallagher
 * @since 2.1.0
 */
@ApplicationScoped
public class DominoFacesImplicitObjectProvider {
	@Produces
	@Dependent
	@Named("database")
	public Database produceDatabase() {
		NotesContext context = NotesContext.getCurrentUnchecked();
		if(context != null) {
			return context.getCurrentDatabase();
		} else {
			return null;
		}
	}

	@Produces
	@Dependent
	@Named("session")
	public Session produceSession() {
		NotesContext context = NotesContext.getCurrentUnchecked();
		if(context != null) {
			return context.getCurrentSession();
		} else {
			return null;
		}
	}

	@Produces
	@Dependent
	@Named("sessionAsSigner")
	public Session produceSessionAsSigner() {
		NotesContext context = NotesContext.getCurrentUnchecked();
		if(context != null) {
			return context.getSessionAsSigner();
		} else {
			return null;
		}
	}

	@Produces
	@Dependent
	@Named("sessionAsSignerWithFullAccess")
	public Session produceSessionAsSignerWithFullAccess() {
		NotesContext context = NotesContext.getCurrentUnchecked();
		if(context != null) {
			return context.getSessionAsSignerFullAdmin();
		} else {
			return null;
		}
	}
}
