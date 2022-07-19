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
package rest;

import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.naming.InitialContext;

import bean.ApplicationGuy;
import bean.RequestGuy;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.StreamingOutput;
import lotus.domino.Database;
import lotus.domino.Session;

@Path("concurrency")
public class ConcurrencyExample {
	
	@Inject
	private RequestGuy requestGuy;

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public StreamingOutput get() {
		return os -> {
			PrintWriter w = new PrintWriter(os);
			try {
				ExecutorService exec = InitialContext.doLookup("java:comp/DefaultManagedExecutorService");
				String basic = exec.submit(() -> "Hello from executor").get();
				w.println(basic);
				
				String cdi = exec.submit(() -> "CDI is " + CDI.current()).get();
				w.println(cdi);
				
				String session = exec.submit(() -> "Username is: " + CDI.current().select(Session.class, NamedLiteral.of("dominoSession")).get().getEffectiveUserName()).get();
				w.println(session);
				
				String database = exec.submit(() -> "Database is: " + CDI.current().select(Database.class).get()).get();
				w.println(database);
				
				String applicationGuy = exec.submit(() -> "applicationGuy is: " + CDI.current().select(ApplicationGuy.class).get().getMessage()).get();
				w.println(applicationGuy);
			} catch(Throwable t) {
				t.printStackTrace(w);
			} finally {
				w.flush();
			}
		};
	}
	
	@Path("scheduled")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public StreamingOutput getScheduled() {
		return os -> {
			PrintWriter w = new PrintWriter(os);
			try {
				w.println("Going to schedule");
				
				String[] val = new String[1];
				ManagedScheduledExecutorService exec = CDI.current().select(ManagedScheduledExecutorService.class).get();
				exec.schedule(() -> { val[0] = "hello from scheduler"; }, 250, TimeUnit.MILLISECONDS);
				Thread.sleep(300);
				
				w.println("Scheduled task provided: " + val[0]);
			} catch(Throwable t) {
				t.printStackTrace(w);
			} finally {
				w.flush();
			}
		};
	}
	
	@Path("requestScope")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public StreamingOutput getRequestScope() {
		return os -> {
			PrintWriter w = new PrintWriter(os);
			try {
				ExecutorService exec = InitialContext.doLookup("java:comp/DefaultManagedExecutorService");
				String basic = exec.submit(() -> "requestGuy is: " + requestGuy.getMessage()).get();
				w.println(basic);
			} catch(Throwable t) {
				t.printStackTrace(w);
			} finally {
				w.flush();
			}
		};
	}
}
