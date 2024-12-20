package org.openntf.xsp.jakarta.validation;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.hibernate.validator.HibernateValidator;
import org.openntf.xsp.jakarta.validation.jndi.DelegatingValidator;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import jakarta.validation.Validation;

public class ValidationActivator implements BundleActivator {
	private static final Logger log = Logger.getLogger(ValidationActivator.class.getPackage().getName());

	@Override
	public void start(BundleContext context) throws Exception {
		InitialContext jndi = new InitialContext();
		try {
			jndi.rebind("java:comp/ValidatorFactory", Validation.byDefaultProvider() //$NON-NLS-1$
					.providerResolver(() -> Arrays.asList(new HibernateValidator()))
					.configure()
					.buildValidatorFactory());
			jndi.rebind("java:comp/Validator", new DelegatingValidator()); //$NON-NLS-1$
		} catch(NamingException e) {
			if(log.isLoggable(Level.SEVERE)) {
				log.log(Level.SEVERE, "Encountered exception binding validators in JNDI", e);
			}
		}
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		
	}

}
