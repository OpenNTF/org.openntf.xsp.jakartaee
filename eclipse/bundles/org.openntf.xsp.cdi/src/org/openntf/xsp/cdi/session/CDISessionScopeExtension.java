package org.openntf.xsp.cdi.session;

import java.io.Serializable;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

/**
 * @since 1.2.0
 */
public class CDISessionScopeExtension implements Extension, Serializable {
	private static final long serialVersionUID = 1L;

	public void addScope(@Observes final BeforeBeanDiscovery event) {
    }

	public void registerContext(@Observes final AfterBeanDiscovery event) {
        event.addContext(new SessionScopeContext());
    }
}
