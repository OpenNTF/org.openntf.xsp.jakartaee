/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	public void start(final BundleContext context) throws Exception {
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
	public void stop(final BundleContext context) throws Exception {

	}

}
