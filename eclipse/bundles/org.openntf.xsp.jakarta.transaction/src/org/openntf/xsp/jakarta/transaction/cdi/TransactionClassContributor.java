package org.openntf.xsp.jakarta.transaction.cdi;

import java.util.Arrays;
import java.util.Collection;

import org.openntf.xsp.cdi.discovery.WeldBeanClassContributor;

import jakarta.enterprise.inject.spi.Extension;

/**
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public class TransactionClassContributor implements WeldBeanClassContributor {

	@Override
	public Collection<Class<?>> getBeanClasses() {
		return Arrays.asList(
			UserTransactionProducer.class
		);
	}

	@Override
	public Collection<Extension> getExtensions() {
		return Arrays.asList(
			new TransactionExtension()
		);
	}

}
