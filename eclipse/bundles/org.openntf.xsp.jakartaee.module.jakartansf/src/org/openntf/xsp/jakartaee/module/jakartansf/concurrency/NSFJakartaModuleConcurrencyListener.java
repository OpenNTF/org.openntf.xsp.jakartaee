package org.openntf.xsp.jakartaee.module.jakartansf.concurrency;

import java.util.Optional;
import java.util.Properties;

import org.openntf.xsp.jakarta.concurrency.AbstractServletConcurrencyContainer;
import org.openntf.xsp.jakartaee.module.jakartansf.NSFJakartaModule;
import org.openntf.xsp.jakartaee.module.jakartansf.util.ActiveRequest;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class NSFJakartaModuleConcurrencyListener extends AbstractServletConcurrencyContainer
		implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ActiveRequest.get().ifPresent(req -> {
			Properties props = LibraryUtil.getXspProperties(req.module());
			this.initializeConcurrencyContainer(props::getProperty);
		});
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		this.terminateConcurrencyContainer();
	}
	
	@Override
	protected Optional<ServletContext> getServletContext() {
		return ActiveRequest.get()
			.map(ActiveRequest::module)
			.map(NSFJakartaModule::getJakartaServletContext);
	}

}
