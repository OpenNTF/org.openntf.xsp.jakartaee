/**
 * Copyright © 2018-2022 Jesse Gallagher
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
package org.openntf.xsp.mvc.bean;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.krazo.cdi.KrazoCdiExtension;
import org.eclipse.krazo.jaxrs.PostMatchingRequestFilter;
import org.eclipse.krazo.jaxrs.PreMatchingRequestFilter;
import org.openntf.xsp.cdi.discovery.WeldBeanClassContributor;
import org.openntf.xsp.mvc.impl.DelegatingExceptionViewEngine;

import jakarta.enterprise.inject.spi.Extension;

public class MvcBeanClassContributor implements WeldBeanClassContributor {

	@Override
	public Collection<Class<?>> getBeanClasses() {
		return Arrays.asList(
			DominoHttpContextBean.class,
			DelegatingExceptionViewEngine.class
		);
	}

	@Override
	public Collection<Extension> getExtensions() {
		return Arrays.asList(new KrazoCdiExtension());
	}
}
