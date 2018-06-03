package org.openntf.xsp.cdi.util;

import javax.enterprise.inject.spi.BeanManager;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.util.ForwardingBeanManager;

import com.ibm.xsp.application.ApplicationEx;

/**
 * Utility methods for working with Weld containers based on a given XPages application.
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public enum ContainerUtil {
	;

	/**
	 * Gets or created a {@link WeldContainer} instance for the provided Application.
	 * 
	 * @param application the active {@link ApplicationEx}
	 * @return an existing or new {@link WeldContainer}
	 */
	public static synchronized WeldContainer getContainer(ApplicationEx application) {
		WeldContainer instance = WeldContainer.instance(application.getApplicationId());
		if(instance == null) {
			instance = new Weld()
				.containerId(application.getApplicationId())
				.property(Weld.SCAN_CLASSPATH_ENTRIES_SYSTEM_PROPERTY, true)
				// Disable concurrent deployment to avoid Notes thread init trouble
				.property("org.jboss.weld.bootstrap.concurrentDeployment", false) //$NON-NLS-1$
				.initialize();
		}
		return instance;
	}

	public static BeanManagerImpl getBeanManager(ApplicationEx application) {
		WeldContainer container = getContainer(application);
		BeanManager manager = container.getBeanManager();
		if(manager instanceof BeanManagerImpl) {
			return (BeanManagerImpl)manager;
		} else if(manager instanceof ForwardingBeanManager) {
			return (BeanManagerImpl) ((ForwardingBeanManager)manager).delegate();
		} else {
			throw new IllegalStateException("Cannot find BeanManagerImpl in " + manager); //$NON-NLS-1$
		}
	}

}
