package org.openntf.xsp.jakarta.persistence;

import java.util.Collections;
import java.util.List;

import jakarta.persistence.spi.PersistenceProvider;
import jakarta.persistence.spi.PersistenceProviderResolver;

public class EclipseLinkResolver implements PersistenceProviderResolver {
	
	private List<PersistenceProvider> providers;
	
	public EclipseLinkResolver() {
		clearCachedProviders();
	}

	@Override
	public List<PersistenceProvider> getPersistenceProviders() {
		return providers;
	}

	@Override
	public void clearCachedProviders() {
		this.providers = Collections.singletonList(new org.eclipse.persistence.jpa.PersistenceProvider());
	}

}
