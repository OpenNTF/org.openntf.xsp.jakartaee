/**
 * Copyright © 2018-2022 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.beanvalidation.cdi;

import org.openntf.xsp.beanvalidation.XPagesValidationUtil;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.validation.ValidatorFactory;

/**
 * Provides an XPages-compatible {@link ValidatorFactory} instance for use in CDI.
 * 
 * @author Jesse Gallagher
 * @since 2.3.0
 */
@ApplicationScoped
public class ValidatorBean {
	@Produces
	public ValidatorFactory produceFactory() {
		return XPagesValidationUtil.constructXPagesValidatorFactory();
	}
}
