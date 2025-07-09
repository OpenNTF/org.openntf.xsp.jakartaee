package org.openntf.xsp.jakarta.batch;

import jakarta.enterprise.inject.spi.Extension;
import java.util.Collection;
import java.util.Set;

import com.ibm.jbatch.spi.services.IBatchConfig;

import org.openntf.xsp.jakarta.cdi.discovery.CDIClassContributor;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class BatchCDIClassContributor implements CDIClassContributor {

	@Override
	public Collection<Class<? extends Extension>> getExtensionClasses() {
		// Try to be tricky and load it from within the bundle
		Bundle jbatchBundle = FrameworkUtil.getBundle(IBatchConfig.class);
		try {
			@SuppressWarnings("unchecked")
			Class<? extends Extension> extensionClass = (Class<? extends Extension>)jbatchBundle.loadClass("com.ibm.jbatch.container.cdi.BatchCDIInjectionExtension"); //$NON-NLS-1$
			return Set.of(extensionClass);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Encountered exception loading JBatch extension", e);
		}
	}

}
