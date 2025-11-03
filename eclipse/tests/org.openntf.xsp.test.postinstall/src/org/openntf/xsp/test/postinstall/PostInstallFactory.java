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
package org.openntf.xsp.test.postinstall;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.ibm.designer.runtime.domino.adapter.HttpService;
import com.ibm.designer.runtime.domino.adapter.IServiceFactory;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import lotus.domino.ACL;
import lotus.domino.ACLEntry;
import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.Session;

/**
 * Performs post-install tasks to configure the JEE example
 * database in the newly-generated Domino container.
 * 
 * @author Jesse Gallagher
 * @since 2.4.0
 */
@SuppressWarnings("nls")
public class PostInstallFactory implements IServiceFactory {

	@Override
	public HttpService[] getServices(LCDEnvironment env) {
		System.out.println("postinstall start");
		
		try {
			// Read the deployed NSFs from the domino-config.json file
			Path configFile = Paths.get("/local/runner/domino-config.json");
			JsonObject configObject;
			try(InputStream is = Files.newInputStream(configFile)) {
				configObject = Json.createReader(is).readObject();
			}
			JsonObject appConfiguration = configObject.getJsonObject("appConfiguration");
			JsonArray applications = appConfiguration.getJsonArray("databases");
			
			Session session = NotesFactory.createSession();
			try {
				for(int i = 0; i < applications.size(); i++) {
					JsonObject dbConfig = applications.getJsonObject(i);
					String nsfName = dbConfig.getString("filePath");
					if(nsfName.startsWith("dev/")) {
						
						// Then it's a good one
						Database database = session.getDatabase("", nsfName);
						if(database == null || !database.isOpen()) {
							throw new RuntimeException("Could not find " + nsfName);
						}
						
						ACL acl = database.getACL();
						ACLEntry anon = acl.getEntry("Anonymous");
						if(anon == null) {
							anon = acl.createACLEntry("Anonymous", ACL.LEVEL_AUTHOR);
						} else {
							anon.setLevel(ACL.LEVEL_AUTHOR);
						}
						
						ACLEntry admin = acl.createACLEntry("CN=Jakarta EE Test/O=OpenNTFTest", ACL.LEVEL_MANAGER);
						try {
							admin.enableRole("[Admin]");
						} catch(NotesException e) {
							// Failing here is fine, in case the NSF doesn't have the role
						}
						
						acl.save();
						
						database.getView("Persons");
						session.sendConsoleCommand("", "load updall " + nsfName);
					}
				}
				
				session.sendConsoleCommand("", "tell http osgi diag");
			} finally {
				session.recycle();
			}
			
			// Try to start JFR if available
			Path jcmd = Paths.get("/opt/hcl/domino/notes/latest/linux/jvm/bin/jcmd");
			if(Files.isExecutable(jcmd)) {
				long pid = ProcessHandle.current().pid();
				System.out.println("Running JFR.start for PID " + pid);
				ProcessBuilder pb = new ProcessBuilder(jcmd.toString(), Long.toString(pid), "JFR.start", "filename=/tmp/flight.jfr");
				Process proc = pb.start();
				proc.waitFor();
			}
			
			System.out.println("Postinstall successful on Java " + System.getProperty("java.version") + " and OSGi version " + System.getProperty("eclipse.buildId"));
		} catch(Throwable e) {
			e.printStackTrace();
		} finally {
			System.out.println("Done with postinstall");
		}
		
		return new HttpService[0];
	}

}
