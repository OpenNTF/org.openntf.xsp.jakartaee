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
package org.openntf.xsp.jakartaee.module.jakartansf;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.IServletFactory;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;
import org.openntf.xsp.jakartaee.module.jakartansf.util.ActiveRequest;
import org.openntf.xsp.jakartaee.module.jakartansf.util.LSXBEHolder;

import lotus.domino.Database;
import lotus.domino.DateTime;
import lotus.domino.Document;
import lotus.domino.NoteCollection;
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
			.map(req -> {
				try {
					Database database = req.lsxbe().database();
					Session sessionAsSigner = req.lsxbe().sessionAsSigner();
					Database databaseAsSigner = sessionAsSigner.getDatabase(database.getServer(), database.getFilePath());

					NoteCollection noteCollection = databaseAsSigner.createNoteCollection(true);
					noteCollection.setSelectSharedFields(true);
					noteCollection.setSelectionFormula("$TITLE=\"$TemplateBuild\""); //$NON-NLS-1$
					noteCollection.buildCollection();
					String noteID = noteCollection.getFirstNoteID();
					if(StringUtil.isNotEmpty(noteID)) {
						Document designDoc = databaseAsSigner.getDocumentByID(noteID);

						if (null != designDoc) {
							String buildVersion = designDoc.getItemValueString("$TemplateBuild"); //$NON-NLS-1$
							Date buildDate = ((DateTime) designDoc.getItemValueDateTimeArray("$TemplateBuildDate").get(0)).toJavaDate(); //$NON-NLS-1$
							String buildDateFormatted = DateFormat.getDateTimeInstance(DateFormat.DEFAULT,DateFormat.DEFAULT).format(buildDate);
							return MessageFormat.format("{0} ({1})", buildVersion, buildDateFormatted); //$NON-NLS-1$
						}
					}

					return null;
				} catch(NotesException e) {
					log.log(Level.ERROR, "Encountered exception trying to read the database template version", e);
					return null;
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
					throw new RuntimeException(e);
				}
			})
			.orElseGet(ComponentModuleLocator.super::getTitle);
	}
}
