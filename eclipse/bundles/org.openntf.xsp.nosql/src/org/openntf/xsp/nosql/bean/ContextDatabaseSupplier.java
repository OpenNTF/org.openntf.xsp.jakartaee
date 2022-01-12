package org.openntf.xsp.nosql.bean;

import org.openntf.xsp.nosql.communication.driver.DatabaseSupplier;

import com.ibm.domino.xsp.module.nsf.NotesContext;

import jakarta.enterprise.context.ApplicationScoped;
import lotus.domino.Database;

@ApplicationScoped
public class ContextDatabaseSupplier implements DatabaseSupplier {

	@Override
	public Database get() {
		return NotesContext.getCurrent().getCurrentDatabase();
	}

}
