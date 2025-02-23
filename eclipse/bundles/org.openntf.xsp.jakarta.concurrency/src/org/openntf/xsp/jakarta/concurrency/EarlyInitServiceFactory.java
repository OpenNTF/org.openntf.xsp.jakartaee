package org.openntf.xsp.jakarta.concurrency;

import com.ibm.designer.runtime.domino.adapter.HttpService;
import com.ibm.designer.runtime.domino.adapter.IServiceFactory;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;

public class EarlyInitServiceFactory implements IServiceFactory {

	@Override
	public HttpService[] getServices(LCDEnvironment env) {
		return new HttpService[0];
	}

}
