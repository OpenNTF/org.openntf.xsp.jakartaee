/**
 * Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakarta.cdi.discovery;

import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.environment.deployment.discovery.BeanArchiveBuilder;

/**
 * This variant of {@link BeanArchiveBuilder} is intended to be a basic
 * holder that represents an empty beans.xml and does not update it
 * when asked to do so.
 *
 * @author Jesse Gallagher
 * @since 2.10.0
 */
public class StaticBeanArchiveBuilder extends BeanArchiveBuilder {
	public StaticBeanArchiveBuilder(final String id) {
		super.setBeansXml(BeansXml.EMPTY_BEANS_XML);
		super.setId(id);
	}

	@Override
	public BeanArchiveBuilder setBeansXml(final BeansXml beansXml) {
		return this;
	}
}
