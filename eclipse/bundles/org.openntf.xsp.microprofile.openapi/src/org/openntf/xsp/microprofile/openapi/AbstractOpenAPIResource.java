/**
 * Copyright © 2018-2022 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.microprofile.openapi;

import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.info.Info;
import org.eclipse.microprofile.openapi.models.servers.Server;
import org.jboss.jandex.Index;
import org.openntf.xsp.jakartaee.DelegatingClassLoader;
import org.openntf.xsp.jaxrs.JAXRSServletFactory;

import com.ibm.commons.util.PathUtil;
import com.ibm.domino.xsp.module.nsf.NotesContext;

import io.smallrye.openapi.api.OpenApiConfig;
import io.smallrye.openapi.api.OpenApiConfigImpl;
import io.smallrye.openapi.api.models.servers.ServerImpl;
import io.smallrye.openapi.runtime.OpenApiProcessor;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.Context;
import lotus.domino.Database;
import lotus.domino.DateTime;
import lotus.domino.Document;
import lotus.domino.NoteCollection;
import lotus.domino.NotesException;
import lotus.domino.Session;

/**
 * @author Jesse Gallagher
 * @since 2.2.0
 */
public abstract class AbstractOpenAPIResource {
	
	@Context
	protected Configuration jaxrsConfig;
	
	@Context
	protected Application application;
	
	@Context
	protected HttpServletRequest req;

	
	protected OpenAPI buildOpenAPI() throws IOException, NotesException {
		Set<Class<?>> classes = new HashSet<>();
		classes.addAll(application.getClasses());
		classes.add(application.getClass());
		Index index = Index.of(classes);
		
		Config mpConfig = CDI.current().select(Config.class).get();
		OpenApiConfig config = OpenApiConfigImpl.fromConfig(mpConfig);
		ClassLoader cl = new DelegatingClassLoader(OpenApiProcessor.class.getClassLoader(), Thread.currentThread().getContextClassLoader());
		OpenAPI openapi;
		synchronized(OpenApiProcessor.class) {
			// OpenApiProcessor appears to be not thread-safe
			openapi = OpenApiProcessor.bootstrap(config, index, cl);
		}
		
		NotesContext notesContext = NotesContext.getCurrent();
		Database database = notesContext.getCurrentDatabase();
		Session sessionAsSigner = notesContext.getSessionAsSigner();
		Database databaseAsSigner = sessionAsSigner.getDatabase(database.getServer(), database.getFilePath());

		Info info = openapi.getInfo();
		String existingTitle = config.getInfoTitle();
		if(existingTitle == null || existingTitle.isEmpty()) {
			info.setTitle(databaseAsSigner.getTitle());
		} else {
			info.setTitle(existingTitle);
		}
		String existingVersion = config.getInfoVersion();
		if(existingVersion == null || existingVersion.isEmpty()) {
			String templateBuild = getVersionNumber(databaseAsSigner);
			if(templateBuild != null && !templateBuild.isEmpty()) {
				info.setVersion(templateBuild);
			} else {
				info.setVersion(existingVersion);
			}
		} else {
			info.setVersion(existingVersion);
		}
	
		// Build a URI to the base of JAX-RS
		Set<String> servers = config.servers();
		if(servers == null || servers.isEmpty()) {
			Server server = new ServerImpl();
			
			URI uri = URI.create(req.getRequestURL().toString());
			String jaxrsRoot = JAXRSServletFactory.getServletPath(notesContext.getModule());
			uri = uri.resolve(PathUtil.concat(req.getContextPath(), jaxrsRoot, '/'));
			String uriString = uri.toString();
			if(uriString.endsWith("/")) { //$NON-NLS-1$
				uriString = uriString.substring(0, uriString.length()-1);
			}
			server.setUrl(uriString);
			openapi.addServer(server);
		}
		
		return openapi;
	}
	
	private static String getVersionNumber(Database database) throws NotesException {
		NoteCollection noteCollection = database.createNoteCollection(true);
		noteCollection.setSelectSharedFields(true);
		noteCollection.setSelectionFormula("$TITLE=\"$TemplateBuild\""); //$NON-NLS-1$
		noteCollection.buildCollection();
		String noteID = noteCollection.getFirstNoteID();
		Document designDoc = database.getDocumentByID(noteID);
		
		if (null != designDoc) {
			String buildVersion = designDoc.getItemValueString("$TemplateBuild"); //$NON-NLS-1$
			Date buildDate = ((DateTime) designDoc.getItemValueDateTimeArray("$TemplateBuildDate").get(0)).toJavaDate(); //$NON-NLS-1$
			String buildDateFormatted = DateFormat.getDateTimeInstance(DateFormat.DEFAULT,DateFormat.DEFAULT).format(buildDate);
			return MessageFormat.format("{0} ({1})", buildVersion, buildDateFormatted); //$NON-NLS-1$
		}
		
		return ""; //$NON-NLS-1$
	}	
}
