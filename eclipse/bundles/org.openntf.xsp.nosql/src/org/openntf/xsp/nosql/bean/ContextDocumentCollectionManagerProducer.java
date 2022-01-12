package org.openntf.xsp.nosql.bean;

import org.openntf.xsp.nosql.communication.driver.DominoDocumentCollectionManager;
import org.openntf.xsp.nosql.communication.driver.DominoDocumentConfiguration;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.nosql.document.DocumentCollectionManagerFactory;
import jakarta.nosql.document.DocumentConfiguration;

@RequestScoped
public class ContextDocumentCollectionManagerProducer {
	private DocumentConfiguration configuration;
	private DocumentCollectionManagerFactory managerFactory;

	@PostConstruct
	public void init() {
		configuration = new DominoDocumentConfiguration();
		managerFactory = configuration.get();
	}
	
	@Produces
	public DominoDocumentCollectionManager getManager() {
		return managerFactory.get(null);
	}
}
