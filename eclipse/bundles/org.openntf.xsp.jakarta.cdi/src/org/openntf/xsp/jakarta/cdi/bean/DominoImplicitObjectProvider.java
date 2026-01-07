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
package org.openntf.xsp.jakarta.cdi.bean;

import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import lotus.domino.Database;
import lotus.domino.Session;

/**
 * This bean provides access to implicit Domino objects from the current
 * module, when available.
 *
 * @author Jesse Gallagher
 * @since 2.1.0
 */
@ApplicationScoped
public class DominoImplicitObjectProvider {
	@Produces
	@Dependent
	@Named("database")
	public Database produceDatabase() {
		return ComponentModuleLocator.getDefault()
			.flatMap(ComponentModuleLocator::getUserDatabase)
			.orElse(null);
	}

	@Produces
	@Dependent
	@Named("dominoSession")
	public Session produceSession() {
		return ComponentModuleLocator.getDefault()
			.flatMap(ComponentModuleLocator::getUserSession)
			.orElse(null);
	}

	@Produces
	@Dependent
	@Named("dominoSessionAsSigner")
	public Session produceSessionAsSigner() {
		return ComponentModuleLocator.getDefault()
			.flatMap(ComponentModuleLocator::getSessionAsSigner)
			.orElse(null);
	}

	@Produces
	@Dependent
	@Named("dominoSessionAsSignerWithFullAccess")
	public Session produceSessionAsSignerWithFullAccess() {
		return ComponentModuleLocator.getDefault()
			.flatMap(ComponentModuleLocator::getSessionAsSignerWithFullAccess)
			.orElse(null);
	}
}
