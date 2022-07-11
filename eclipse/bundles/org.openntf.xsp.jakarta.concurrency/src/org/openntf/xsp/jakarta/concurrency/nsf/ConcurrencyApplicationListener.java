package org.openntf.xsp.jakarta.concurrency.nsf;

import java.util.Optional;

import org.openntf.xsp.jakarta.concurrency.AbstractServletConcurrencyContainer;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;

import com.ibm.domino.xsp.module.nsf.NSFComponentModule;
import com.ibm.domino.xsp.module.nsf.NotesContext;
import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.application.events.ApplicationListener2;

import jakarta.servlet.ServletContext;

/**
 * Listens for XPages application initialization - which should be roughly
 * comparable to ServletContext lifecycle - to attach a listener to init/term
 * JNDI configuration for Concurrency.
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public class ConcurrencyApplicationListener extends AbstractServletConcurrencyContainer implements ApplicationListener2 {
	@Override
	public void applicationCreated(ApplicationEx app) {
		initializeConcurrencyContainer();
	}

	@Override
	public void applicationDestroyed(ApplicationEx app) {
		terminateConcurrencyContainer();
	}

	@Override
	public void applicationRefreshed(ApplicationEx app) {
		
	}
	
	private Optional<NSFComponentModule> getModule() {
		NotesContext ctx = NotesContext.getCurrentUnchecked();
		if(ctx != null) {
			return Optional.ofNullable(ctx.getModule());
		}
		return Optional.empty();
	}

	@Override
	protected Optional<ServletContext> getServletContext() {
		return getModule()
			.map(module -> {
				javax.servlet.ServletContext oldContext = module.getServletContext();
				ServletContext servletContext = ServletUtil.oldToNew(module.getDatabasePath(), oldContext);
				return servletContext;
			});
	}
}
