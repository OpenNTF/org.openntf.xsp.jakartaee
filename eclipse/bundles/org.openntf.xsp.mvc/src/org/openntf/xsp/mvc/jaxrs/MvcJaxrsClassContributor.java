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
package org.openntf.xsp.mvc.jaxrs;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.krazo.binding.convert.MvcConverterProvider;
import org.eclipse.krazo.core.ViewResponseFilter;
import org.eclipse.krazo.core.ViewableWriter;
import org.eclipse.krazo.jaxrs.PostMatchingRequestFilter;
import org.eclipse.krazo.jaxrs.PreMatchingRequestFilter;
import org.openntf.xsp.jaxrs.JAXRSClassContributor;

public class MvcJaxrsClassContributor implements JAXRSClassContributor {

	@Override
	public Collection<Class<?>> getClasses() {
		return Arrays.asList(
			PreMatchingRequestFilter.class,
			PostMatchingRequestFilter.class,
			ViewableWriter.class,
			ViewResponseFilter.class,
			MvcConverterProvider.class
		);
	}

}
