package org.openntf.xsp.jakarta.rest.client;

import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.api.ClientBuilderConfiguration;
import org.jboss.resteasy.client.jaxrs.engine.ClientHttpEngineFactory;
import org.jboss.resteasy.client.jaxrs.engines.AsyncClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.engines.URLConnectionEngine;

public class URLConnectionClientHttpEngineFactory implements ClientHttpEngineFactory {

	@Override
	public ClientHttpEngine httpClientEngine(final ClientBuilderConfiguration configuration) {
		return new URLConnectionEngine();
	}

	@Override
	public AsyncClientHttpEngine asyncHttpClientEngine(final ClientBuilderConfiguration configuration) {
		throw new UnsupportedOperationException();
	}

}
