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
package org.openntf.xsp.jakarta.cdi.discovery;

import java.util.UUID;

import org.jboss.weld.environment.deployment.discovery.BeanArchiveBuilder;
import org.jboss.weld.environment.deployment.discovery.BeanArchiveHandler;

import jakarta.annotation.Priority;

/**
 * This implementation of {@link BeanArchiveHandler} just returns a
 * basic {@link BeanArchiveBuilder} with a randomized ID, since actual
 * class contribution happens elsewhere.
 *
 * @author Jesse Gallagher
 * @since 2.10.0
 */
@Priority(Integer.MAX_VALUE)
public class StaticBeanArchiveHandler implements BeanArchiveHandler {

	@Override
	public BeanArchiveBuilder handle(final String beanArchiveReference) {
		return new StaticBeanArchiveBuilder(UUID.randomUUID().toString());
	}

}
