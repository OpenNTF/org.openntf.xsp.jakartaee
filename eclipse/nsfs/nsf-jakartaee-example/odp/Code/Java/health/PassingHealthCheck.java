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
package health;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.HealthCheckResponseBuilder;
import org.eclipse.microprofile.health.Liveness;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import lotus.domino.Database;
import lotus.domino.NoteCollection;
import lotus.domino.NotesException;

@RequestScoped
@Liveness
public class PassingHealthCheck implements HealthCheck {
	
	@Inject
	private Database database;

	@Override
	public HealthCheckResponse call() {
		HealthCheckResponseBuilder response = HealthCheckResponse.named("I am the liveliness check");
		try {
			NoteCollection notes = database.createNoteCollection(true);
			notes.buildCollection();
			return response
				.status(true)
				.withData("noteCount", notes.getCount())
				.build();
		} catch(NotesException e) {
			String stackTrace = null;
			try(StringWriter w = new StringWriter(); PrintWriter pw = new PrintWriter(w)) {
				e.printStackTrace(pw);
				stackTrace = w.toString();
			} catch(IOException ignore) { }
			return response
				.status(false)
				.withData("exception", e.text)
				.withData("stackTrace", stackTrace)
				.build();
		}
	}

}
