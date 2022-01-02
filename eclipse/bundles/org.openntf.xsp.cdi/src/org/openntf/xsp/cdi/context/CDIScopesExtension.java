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
package org.openntf.xsp.cdi.context;

import java.io.Serializable;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;

/**
 * @since 1.2.0
 */
public class CDIScopesExtension implements Extension, Serializable {
	private static final long serialVersionUID = 1L;

	public void addScope(@Observes final BeforeBeanDiscovery event) {
    }

	public void registerContext(@Observes final AfterBeanDiscovery event) {
        event.addContext(new SessionScopeContext());
        event.addContext(new RequestScopeContext());
        event.addContext(new ConversationScopeContext());
    }
}
