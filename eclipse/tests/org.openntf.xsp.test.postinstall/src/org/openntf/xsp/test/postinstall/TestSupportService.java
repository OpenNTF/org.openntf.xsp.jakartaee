package org.openntf.xsp.test.postinstall;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.servlet.ServletException;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.HttpService;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletRequestAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletResponseAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpSessionAdapter;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import lotus.domino.ACL;
import lotus.domino.ACLEntry;
import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.Session;

/**
 * @since 3.7.0
 */
@SuppressWarnings("nls")
public class TestSupportService extends HttpService {

	public TestSupportService(LCDEnvironment paramLCDEnvironment) {
		super(paramLCDEnvironment);
		
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
						anon.setPublicWriter(true);
						
						ACLEntry admin = acl.createACLEntry("CN=Jakarta EE Test/O=OpenNTFTest", ACL.LEVEL_MANAGER);
						try {
							admin.enableRole("[Admin]");
						} catch(NotesException e) {
							// Failing here is fine, in case the NSF doesn't have the role
						}
						
						acl.save();
						
						// Make sure the design collection is good
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
	}
	
	@Override
	public boolean isXspUrl(String fullPath, boolean arg1) {
		return String.valueOf(fullPath).contains("doFinalShutdownTasks");
	}

	@Override
	public boolean doService(String lcdContextPath, String pathInfo, HttpSessionAdapter session, HttpServletRequestAdapter request,
			HttpServletResponseAdapter response) throws ServletException, IOException {
		try {
			if(String.valueOf(pathInfo).contains("doFinalShutdownTasks")) {
				new ProcessBuilder("tar", "-czvf", "/tmp/IBM_TECHNICAL_SUPPORT.tar.gz", "/local/notesdata/IBM_TECHNICAL_SUPPORT")
						.inheritIO()
						.start()
						.waitFor();
				new ProcessBuilder("tar", "-czvf", "/tmp/workspace-logs.tar.gz", "/local/notesdata/domino/workspace/logs")
						.inheritIO()
						.start()
						.waitFor();
				new ProcessBuilder("tar", "-czvf", "/tmp/devnsfs.tar.gz", "/local/notesdata/dev")
						.inheritIO()
						.start()
						.waitFor();
				
				Path jcmd = Paths.get("/opt/hcl/domino/notes/latest/linux/jvm/bin/jcmd");
				if(Files.isExecutable(jcmd)) {
					long pid = ProcessHandle.current().pid();
					new ProcessBuilder("/opt/hcl/domino/notes/latest/linux/jvm/bin/jcmd", String.valueOf(pid), "JFR.dump")
						.inheritIO()
						.start()
						.waitFor();
				}
				
				// Now stitch together a MIME response of the files
				MimeMultipart result = new MimeMultipart();
				result.setSubType("form-data");
				{
					MimeBodyPart part = new MimeBodyPart();
					part.setFileName("IBM_TECHNICAL_SUPPORT.tar.gz");
					Path file = Paths.get("/tmp/IBM_TECHNICAL_SUPPORT.tar.gz");
					var content = Files.readAllBytes(file);
					part.setContent(content, "application/octet-stream");
					result.addBodyPart(part);
				}
				{
					MimeBodyPart part = new MimeBodyPart();
					part.setFileName("workspace-logs.tar.gz");
					Path file = Paths.get("/tmp/workspace-logs.tar.gz");
					var content = Files.readAllBytes(file);
					part.setContent(content, "application/octet-stream");
					result.addBodyPart(part);
				}
				{
					MimeBodyPart part = new MimeBodyPart();
					part.setFileName("devnsfs.tar.gz");
					Path file = Paths.get("/tmp/devnsfs.tar.gz");
					var content = Files.readAllBytes(file);
					part.setContent(content, "application/octet-stream");
					result.addBodyPart(part);
				}
				{
					Path file = Paths.get("/tmp/flight.jfr");
					if(Files.exists(file)) {
						MimeBodyPart part = new MimeBodyPart();
						part.setFileName("flight.jfr");
						var content = Files.readAllBytes(file);
						part.setContent(content, "application/octet-stream");
						result.addBodyPart(part);
					}
				}
				
				response.setContentType("multipart/form-data");
				var os = response.getOutputStream();
				result.writeTo(os);
				
				return true;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void getModules(List<ComponentModule> modules) {
		
	}

}
