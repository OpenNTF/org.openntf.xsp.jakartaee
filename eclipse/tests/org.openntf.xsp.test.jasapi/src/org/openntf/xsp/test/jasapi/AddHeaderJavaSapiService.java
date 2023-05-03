package org.openntf.xsp.test.jasapi;

import com.ibm.domino.bridge.http.jasapi.IJavaSapiEnvironment;
import com.ibm.domino.bridge.http.jasapi.IJavaSapiHttpContextAdapter;
import com.ibm.domino.bridge.http.jasapi.JavaSapiService;

public class AddHeaderJavaSapiService extends JavaSapiService {

	public AddHeaderJavaSapiService(IJavaSapiEnvironment env) {
		super(env);
	}

	@Override
	public int authenticate(IJavaSapiHttpContextAdapter context) {
		return HTEXTENSION_EVENT_DECLINED;
	}

	@Override
	public void startRequest(IJavaSapiHttpContextAdapter context) {
		
	}

	@Override
	public void endRequest(IJavaSapiHttpContextAdapter context) {
		
	}

	@Override
	public String getServiceName() {
		return getClass().getSimpleName();
	}

	@Override
	public int processRequest(IJavaSapiHttpContextAdapter context) {
		return HTEXTENSION_EVENT_DECLINED;
	}

	@Override
	public int rawRequest(IJavaSapiHttpContextAdapter context) {
		context.getResponse().setHeader("X-AddHeaderJavaSapiService", "Hello"); //$NON-NLS-1$ //$NON-NLS-2$
		return HTEXTENSION_SUCCESS;
	}

	@Override
	public int rewriteURL(IJavaSapiHttpContextAdapter context) {
		return HTEXTENSION_EVENT_DECLINED;
	}

}
