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
	public static final String[] NSFS = {
		"dev/jakartaee.nsf",
		"dev/jeebundle.nsf"
	};

	@Override
	public HttpService[] getServices(LCDEnvironment env) {
		System.out.println("postinstall start");
		
		try {
			Session session = NotesFactory.createSession();
			try {
				for(String nsfName : NSFS) {
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
				
				session.sendConsoleCommand("", "tell http osgi diag");
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
