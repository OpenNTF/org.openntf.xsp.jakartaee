package org.openntf.xsp.jaxrs.weld;

import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.server.spi.ExternalRequestContext;
import org.glassfish.jersey.server.spi.ExternalRequestScope;

public class NoopExternalRequestScope implements ExternalRequestScope<Object> {

	@Override
    public ExternalRequestContext<Object> open(InjectionManager injectionManager) {
        return null;
    }

    @Override
    public void close() {
    }

    @Override
    public void suspend(ExternalRequestContext<Object> o, InjectionManager injectionManager) {
    }

    @Override
    public void resume(ExternalRequestContext<Object> o, InjectionManager injectionManager) {
    }

}
