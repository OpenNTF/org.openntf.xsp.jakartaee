package org.openntf.xsp.jakarta.bridge.jasapi.test;

import java.util.Arrays;

import com.ibm.domino.bridge.http.jasapi.IJavaSapiEnvironment;
import com.ibm.domino.bridge.http.jasapi.IJavaSapiHttpContextAdapter;
import com.ibm.domino.bridge.http.jasapi.JavaSapiService;

public class JakartaSapiService extends JavaSapiService {

	public JakartaSapiService(IJavaSapiEnvironment env) {
		super(env);
	}

	@Override
	public String getServiceName() {
		return getClass().getSimpleName();
	}

	@Override
	public int authenticate(IJavaSapiHttpContextAdapter context) {
		//context.getRequest().setAuthenticatedUserName("CN=Hello From " + getClass().getName(), getClass().getSimpleName());
		//return HTEXTENSION_REQUEST_AUTHENTICATED;
		return HTEXTENSION_EVENT_DECLINED;
	}

	@Override
	public void startRequest(IJavaSapiHttpContextAdapter context) {
	}

	@Override
	public int processRequest(IJavaSapiHttpContextAdapter context) {
		return HTEXTENSION_EVENT_DECLINED;
	}

	@Override
	public void endRequest(IJavaSapiHttpContextAdapter context) {
	}

	@Override
	public int rawRequest(IJavaSapiHttpContextAdapter context) {
		System.out.println(">> rawRequest " + context);
		
		// Can add headers here
		
		return HTEXTENSION_EVENT_HANDLED;
	}

	@Override
	public int rewriteURL(IJavaSapiHttpContextAdapter context) {
		return HTEXTENSION_EVENT_DECLINED;
	}
	
	@Override
	public int processConsoleCommand(String[] argv, int argc) {
		if(argc > 0) {
			if("jakarta".equals(argv[0])) { //$NON-NLS-1$
				System.out.println(getClass().getSimpleName() + " was told " + Arrays.toString(argv));
				return HTEXTENSION_SUCCESS;
			}
		}
		return HTEXTENSION_EVENT_DECLINED;
	}

}
