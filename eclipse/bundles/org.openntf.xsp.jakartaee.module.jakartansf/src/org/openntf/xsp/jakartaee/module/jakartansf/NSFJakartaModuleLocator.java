/*
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
package org.openntf.xsp.jakartaee.module.jakartansf;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Optional;

import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;
import org.openntf.xsp.jakartaee.module.jakartansf.util.ActiveRequest;
import org.openntf.xsp.jakartaee.module.jakartansf.util.LSXBEHolder;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.jakartaee.util.UncheckedNotesException;

import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.IServletFactory;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;

public class NSFJakartaModuleLocator implements ComponentModuleLocator {
	private static final Logger log = System.getLogger(NSFJakartaModuleLocator.class.getPackageName());

	@Override
	public boolean isActive() {
		return ActiveRequest.get().isPresent();
	}

	@Override
	public ComponentModule getActiveModule() {
		return ActiveRequest.get()
			.map(ActiveRequest::module)
			.orElse(null);
	}

	@Override
	public Optional<String> getVersion() {
		return ActiveRequest.get()
			.flatMap(req -> {
				try {
					Database database = req.lsxbe().database();
					Session sessionAsSigner = req.lsxbe().sessionAsSigner();
					Database databaseAsSigner = sessionAsSigner.getDatabase(database.getServer(), database.getFilePath());
					try {
						return LibraryUtil.getDatabaseVersion(databaseAsSigner);
					} finally {
						databaseAsSigner.recycle();
					}
				} catch(NotesException e) {
					log.log(Level.ERROR, () -> MessageFormat.format("Encountered exception trying to read the database template version (Status: 0x{0})", Integer.toHexString(e.id)), e);
					return Optional.empty();
				}
			});
	}

	@Override
	public Optional<NotesDatabase> getNotesDatabase() {
		return ActiveRequest.get()
			.map(ActiveRequest::module)
			.map(NSFJakartaModule::getNotesDatabase);
	}

	@Override
	public Optional<Database> getUserDatabase() {
		return ActiveRequest.get()
			.map(ActiveRequest::lsxbe)
			.map(LSXBEHolder::database);
	}

	@Override
	public Optional<Session> getUserSession() {
		return ActiveRequest.get()
			.map(ActiveRequest::lsxbe)
			.map(LSXBEHolder::session);
	}

	@Override
	public Optional<Session> getSessionAsSigner() {
		return ActiveRequest.get()
			.map(ActiveRequest::lsxbe)
			.map(LSXBEHolder::sessionAsSigner);
	}

	@Override
	public Optional<Session> getSessionAsSignerWithFullAccess() {
		return ActiveRequest.get()
			.map(ActiveRequest::lsxbe)
			.map(LSXBEHolder::sessionAsSignerFullAccess);
	}

	@Override
	public Optional<ServletContext> getServletContext() {
		return ActiveRequest.get()
			.map(ActiveRequest::module)
			.map(NSFJakartaModule::getJakartaServletContext);
	}

	@Override
	public Optional<HttpServletRequest> getServletRequest() {
		return ActiveRequest.get()
			.map(ActiveRequest::request);
	}

	@Override
	public Collection<? extends IServletFactory> getServletFactories() {
		return ActiveRequest.get()
			.map(ActiveRequest::module)
			.map(NSFJakartaModule::getServletFactories)
			.orElse(null);
	}
	
	@Override
	public String getTitle() {
		return getUserDatabase()
			.map(db -> {
				try {
					return db.getTitle();
				} catch (NotesException e) {
					throw new UncheckedNotesException(e);
				}
			})
			.orElseGet(ComponentModuleLocator.super::getTitle);
	}
}
