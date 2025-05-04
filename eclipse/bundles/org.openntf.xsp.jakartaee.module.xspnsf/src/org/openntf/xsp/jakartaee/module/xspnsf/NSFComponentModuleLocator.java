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
package org.openntf.xsp.jakartaee.module.xspnsf;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.domino.xsp.module.nsf.NSFComponentModule;
import com.ibm.domino.xsp.module.nsf.NotesContext;

import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;
import org.openntf.xsp.jakartaee.module.xspnsf.concurrency.NSFSessionClonerSetupParticipant;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;

import jakarta.annotation.Priority;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import lotus.domino.Database;
import lotus.domino.DateTime;
import lotus.domino.Document;
import lotus.domino.NoteCollection;
import lotus.domino.NotesException;
import lotus.domino.Session;

/**
 * Locates an active {@link NSFComponentModule} when the current request
 * is in an NSF context.
 *
 * @author Jesse Gallagher
 * @since 2.8.0
 */
@Priority(1)
public class NSFComponentModuleLocator implements ComponentModuleLocator {
	private static final Field notesContextRequestField;
	static {
		notesContextRequestField = AccessController.doPrivileged((PrivilegedAction<Field>)() -> {
			try {
				Field field = NotesContext.class.getDeclaredField("httpRequest"); //$NON-NLS-1$
				field.setAccessible(true);
				return field;
			} catch (NoSuchFieldException | SecurityException e) {
				throw new RuntimeException(e);
			}
		});
	}
	private static final Logger log = Logger.getLogger(NSFComponentModuleLocator.class.getPackage().getName());

	@Override
	public boolean isActive() {
		return NotesContext.getCurrentUnchecked() != null;
	}

	@Override
	public NSFComponentModule getActiveModule() {
		NotesContext nsfContext = NotesContext.getCurrentUnchecked();
		if(nsfContext != null) {
			return nsfContext.getModule();
		}
		return null;
	}

	@Override
	public Optional<ServletContext> getServletContext() {
		NotesContext nsfContext = NotesContext.getCurrentUnchecked();
		if(nsfContext != null) {
			NSFComponentModule module = nsfContext.getModule();
			String path = module.getDatabasePath().replace('\\', '/');
			javax.servlet.ServletContext servletContext = module.getServletContext();
			return Optional.of(ServletUtil.oldToNew(path, servletContext));
		}
		return Optional.empty();
	}

	@Override
	public Optional<HttpServletRequest> getServletRequest() {
		return getServletContext()
			.flatMap(servletContext -> {
				NotesContext nsfContext = NotesContext.getCurrentUnchecked();
				if(nsfContext != null) {
					try {
						javax.servlet.http.HttpServletRequest request = (javax.servlet.http.HttpServletRequest)notesContextRequestField.get(nsfContext);
						return Optional.ofNullable(ServletUtil.oldToNew(ServletUtil.newToOld(servletContext), request));
					} catch (IllegalArgumentException | IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}
				return Optional.empty();
			});
	}

	@Override
	public String getTitle() {
		NotesContext nsfContext = NotesContext.getCurrentUnchecked();
		if(nsfContext != null) {
			try {
				return nsfContext.getCurrentDatabase().getTitle();
			} catch (NotesException e) {
				if(log.isLoggable(Level.SEVERE)) {
					log.log(Level.SEVERE, "Encountered exception trying to read the database title", e);
				}
				return getActiveModule().getModuleName();
			}
		}
		return getActiveModule().getModuleName();
	}

	@Override
	public Optional<String> getVersion() {
		NotesContext nsfContext = NotesContext.getCurrentUnchecked();
		if(nsfContext != null) {
			try {
				Database database = nsfContext.getCurrentDatabase();
				Session sessionAsSigner = nsfContext.getSessionAsSigner();
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
						return Optional.of(MessageFormat.format("{0} ({1})", buildVersion, buildDateFormatted)); //$NON-NLS-1$
					}
				}

				return Optional.empty();
			} catch(NotesException e) {
				if(log.isLoggable(Level.SEVERE)) {
					log.log(Level.SEVERE, "Encountered exception trying to read the database template version", e);
				}
				return Optional.empty();
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<NotesDatabase> getNotesDatabase() {
		NotesContext nsfContext = NotesContext.getCurrentUnchecked();
		if(nsfContext != null) {
			try {
				return Optional.of(nsfContext.getNotesDatabase());
			} catch (NotesAPIException e) {
				if(log.isLoggable(Level.SEVERE)) {
					log.log(Level.SEVERE, "Encountered exception trying to open the context database", e);
				}
				return Optional.empty();
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<Database> getUserDatabase() {
		NotesContext nsfContext = NotesContext.getCurrentUnchecked();
		if(nsfContext != null) {
			return Optional.ofNullable(nsfContext.getCurrentDatabase());
		}
		return Optional.empty();
	}

	@Override
	public Optional<Session> getUserSession() {
		NotesContext nsfContext = NotesContext.getCurrentUnchecked();
		if(nsfContext != null) {
			return Optional.ofNullable(nsfContext.getCurrentSession());
		}
		return Optional.empty();
	}

	@Override
	public Optional<Session> getSessionAsSigner() {
		Session threadSession = NSFSessionClonerSetupParticipant.THREAD_SESSIONASSIGNER.get();
		if(threadSession != null) {
			return Optional.of(threadSession);
		}
		
		NotesContext nsfContext = NotesContext.getCurrentUnchecked();
		if(nsfContext != null) {
			return Optional.ofNullable(nsfContext.getSessionAsSigner());
		}
		return Optional.empty();
	}

	@Override
	public Optional<Session> getSessionAsSignerWithFullAccess() {
		Session threadSession = NSFSessionClonerSetupParticipant.THREAD_SESSIONASSIGNER.get();
		if(threadSession != null) {
			return Optional.of(threadSession);
		}

		NotesContext nsfContext = NotesContext.getCurrentUnchecked();
		if(nsfContext != null) {
			return Optional.ofNullable(nsfContext.getSessionAsSignerFullAdmin());
		}
		return Optional.empty();
	}

}
