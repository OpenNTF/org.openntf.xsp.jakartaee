package org.openntf.xsp.jakarta.bridge.jasapi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.ibm.domino.oauth.provider.OAuthService;
import com.ibm.domino.oauth.provider.OAuthServiceException;

public class JakartaOAuthService implements OAuthService {
	
	public JakartaOAuthService() {
		System.out.println("JakartaOAuthService init!");
	}

	@Override
	public void configureFromProps(String arg0, boolean arg1, boolean arg2) throws OAuthServiceException {
		System.out.println("JakartaOAuthService asked to configure from props: arg0=" + arg0 + ", arg1=" + arg1 + ", arg2=" + arg2);
	}

	@Override
	public String getId() {
		System.out.println("JakartaOAuthService asked for id");
		return getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		System.out.println("JakartaOAuthService asked for title");
		return getClass().getSimpleName();
	}

	@Override
	public Map<String, String[]> getUrls() {
		System.out.println("JakartaOAuthService asked for URLs");
		Map<String, String[]> result = new HashMap<>();
//		result.put("^.*$", new String[] { "foo", "bar" });
		return result;
	}

}
