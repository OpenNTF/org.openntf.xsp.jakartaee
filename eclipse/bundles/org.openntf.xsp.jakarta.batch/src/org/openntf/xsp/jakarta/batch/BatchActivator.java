package org.openntf.xsp.jakarta.batch;

import com.ibm.jbatch.spi.BatchSPIManager;
import org.openntf.xsp.jakarta.concurrency.jndi.DelegatingManagedExecutorService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import jakarta.batch.runtime.BatchRuntime;

public class BatchActivator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		DelegatingManagedExecutorService svc = new DelegatingManagedExecutorService();
		BatchSPIManager.getInstance().registerExecutorServiceProvider(() -> svc);
		
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		
	}

}
