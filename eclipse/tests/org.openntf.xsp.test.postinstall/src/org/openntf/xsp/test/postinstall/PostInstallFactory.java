package org.openntf.xsp.test.postinstall;

import com.ibm.designer.runtime.domino.adapter.HttpService;
import com.ibm.designer.runtime.domino.adapter.IServiceFactory;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;

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
			Session session = NotesFactory.createSession();
			try {
				Database database = session.getDatabase("", "dev/jakartaee.nsf");
				if(database == null || !database.isOpen()) {
					throw new RuntimeException("Could not find jakartaee.nsf");
				}
				
				ACL acl = database.getACL();
				ACLEntry anon = acl.getEntry("Anonymous");
				if(anon == null) {
					anon = acl.createACLEntry("Anonymous", ACL.LEVEL_AUTHOR);
				} else {
					anon.setLevel(ACL.LEVEL_AUTHOR);
				}
				
				ACLEntry admin = acl.createACLEntry("CN=Jakarta EE Test/O=OpenNTFTest", ACL.LEVEL_MANAGER);
				admin.enableRole("[Admin]");
				
				acl.save();
				
				database.getView("Persons");
				session.sendConsoleCommand("", "load updall dev/jakartaee.nsf");
			} finally {
				session.recycle();
			}
		} catch(NotesException e) {
			e.printStackTrace();
		} finally {
			System.out.println("Done with postinstall");
		}
		
		return new HttpService[0];
	}

}
