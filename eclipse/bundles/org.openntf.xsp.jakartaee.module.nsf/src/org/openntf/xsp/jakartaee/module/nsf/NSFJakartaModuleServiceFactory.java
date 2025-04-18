package org.openntf.xsp.jakartaee.module.nsf;

import com.ibm.designer.runtime.domino.adapter.HttpService;
import com.ibm.designer.runtime.domino.adapter.IServiceFactory;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;

/**
 * @since 3.4.0
 */
public class NSFJakartaModuleServiceFactory implements IServiceFactory {

	@Override
	public HttpService[] getServices(LCDEnvironment env) {
		return new HttpService[] { new NSFJakartaModuleService(env) };
	}

}
