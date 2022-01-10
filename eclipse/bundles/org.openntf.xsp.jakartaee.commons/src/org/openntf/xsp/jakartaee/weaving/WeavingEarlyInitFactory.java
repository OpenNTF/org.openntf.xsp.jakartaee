package org.openntf.xsp.jakartaee.weaving;

import com.ibm.designer.runtime.domino.adapter.HttpService;
import com.ibm.designer.runtime.domino.adapter.IServiceFactory;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;

/**
 * This stub {@link IServiceFactory} implementation exists solely to
 * ensure that this bundle is activated early in HTTP initialization
 * to get {@link UtilWeavingHook} in operation.
 * 
 * @author Jesse Gallagher
 * @since 2.2.0
 */
public class WeavingEarlyInitFactory implements IServiceFactory {
	@Override
	public HttpService[] getServices(LCDEnvironment env) {
		return new HttpService[0];
	}
}
