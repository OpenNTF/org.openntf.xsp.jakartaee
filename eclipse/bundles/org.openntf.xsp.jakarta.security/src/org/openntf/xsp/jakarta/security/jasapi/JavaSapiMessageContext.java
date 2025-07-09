package org.openntf.xsp.jakarta.security.jasapi;

import java.security.Principal;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;

import org.openntf.xsp.jakartaee.jasapi.JavaSapiContext;

import com.ibm.commons.util.StringUtil;

import jakarta.security.auth.message.MessageInfo;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.AuthenticationParameters;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.security.enterprise.identitystore.CredentialValidationResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JavaSapiMessageContext implements HttpMessageContext {
	private final JavaSapiContext delegate;
	
	public JavaSapiMessageContext(JavaSapiContext delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean isProtected() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAuthenticationRequest() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRegisterSession() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setRegisterSession(String callerName, Set<String> groups) {
		// TODO Auto-generated method stub

	}

	@Override
	public void cleanClientSubject() {
		// TODO Auto-generated method stub

	}

	@Override
	public AuthenticationParameters getAuthParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CallbackHandler getHandler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Subject getClientSubject() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpServletRequest getRequest() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setRequest(HttpServletRequest request) {
		// TODO Auto-generated method stub

	}

	@Override
	public HttpMessageContext withRequest(HttpServletRequest request) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpServletResponse getResponse() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setResponse(HttpServletResponse response) {
		// TODO Auto-generated method stub

	}

	@Override
	public AuthenticationStatus redirect(String location) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AuthenticationStatus forward(String path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AuthenticationStatus responseUnauthorized() {
		return AuthenticationStatus.SEND_FAILURE;
	}

	@Override
	public AuthenticationStatus responseNotFound() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AuthenticationStatus notifyContainerAboutLogin(String callername, Set<String> groups) {
		return notifyContainerAboutLogin(() -> callername, groups);
	}

	@Override
	public AuthenticationStatus notifyContainerAboutLogin(Principal principal, Set<String> groups) {
		if(StringUtil.isNotEmpty(principal.getName())) {
			this.delegate.getRequest().setAuthenticatedUserName(principal.getName(), "Basic");
			
			return AuthenticationStatus.SUCCESS;
		} else {
			return AuthenticationStatus.NOT_DONE;
		}
	}

	@Override
	public AuthenticationStatus notifyContainerAboutLogin(CredentialValidationResult result) {
		return notifyContainerAboutLogin(result.getCallerPrincipal(), result.getCallerGroups());
	}

	@Override
	public AuthenticationStatus doNothing() {
		return AuthenticationStatus.NOT_DONE;
	}

	@Override
	public Principal getCallerPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<String> getGroups() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MessageInfo getMessageInfo() {
		// TODO Auto-generated method stub
		return null;
	}

}