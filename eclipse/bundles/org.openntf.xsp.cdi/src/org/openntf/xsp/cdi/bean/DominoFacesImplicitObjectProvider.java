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
package org.openntf.xsp.cdi.bean;

import javax.faces.context.FacesContext;

import com.ibm.domino.xsp.module.nsf.NotesContext;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import lotus.domino.Database;
import lotus.domino.Session;

/**
 * This bean provides access to implicit objects from the current
 * {@link FacesContext}, when available.
 * 
 * @author Jesse Gallagher
 * @since 2.1.0
 */
@ApplicationScoped
public class DominoFacesImplicitObjectProvider {
	@Produces
	@Dependent
	@Named("database")
	public Database produceDatabase() {
		NotesContext context = NotesContext.getCurrentUnchecked();
		if(context != null) {
			return context.getCurrentDatabase();
		} else {
			return null;
		}
	}

	@Produces
	@Dependent
	@Named("dominoSession")
	public Session produceSession() {
		NotesContext context = NotesContext.getCurrentUnchecked();
		if(context != null) {
			return context.getCurrentSession();
		} else {
			return null;
		}
	}

	@Produces
	@Dependent
	@Named("dominoSessionAsSigner")
	public Session produceSessionAsSigner() {
		NotesContext context = NotesContext.getCurrentUnchecked();
		if(context != null) {
			return context.getSessionAsSigner();
		} else {
			return null;
		}
	}

	@Produces
	@Dependent
	@Named("dominoSessionAsSignerWithFullAccess")
	public Session produceSessionAsSignerWithFullAccess() {
		NotesContext context = NotesContext.getCurrentUnchecked();
		if(context != null) {
			return context.getSessionAsSignerFullAdmin();
		} else {
			return null;
		}
	}
}
