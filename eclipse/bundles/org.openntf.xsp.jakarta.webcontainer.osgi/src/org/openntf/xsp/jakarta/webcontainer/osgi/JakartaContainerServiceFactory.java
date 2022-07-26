package org.openntf.xsp.jakarta.webcontainer.osgi;

import com.ibm.designer.runtime.domino.adapter.HttpService;
import com.ibm.designer.runtime.domino.adapter.IServiceFactory;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;

/**
 * 
 * @author Jesse Gallagher
 * @since 2.8.0
 */
public class JakartaContainerServiceFactory implements IServiceFactory {

	@Override
	public HttpService[] getServices(LCDEnvironment env) {
		return new HttpService[] {
			new JakartaContainerService(env)
		};
	}

}
