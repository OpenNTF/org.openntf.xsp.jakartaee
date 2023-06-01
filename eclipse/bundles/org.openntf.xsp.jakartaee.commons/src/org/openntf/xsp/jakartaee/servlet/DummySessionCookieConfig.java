package org.openntf.xsp.jakartaee.servlet;

import java.util.Collections;
import java.util.Map;

import jakarta.servlet.SessionCookieConfig;

/**
 * Basic implementation of {@link SessionCookieConfig} that no-ops a number
 * of methods, as this isn't controllable on Domino
 * 
 * @author Jesse Gallagher
 * @since 3.0.0
 */
// TODO consider looking up some values from server config if practical
public class DummySessionCookieConfig implements SessionCookieConfig {
	@Override
	public void setName(String name) {
		
	}

	@Override
	public String getName() {
		return "JSESSIONID"; //$NON-NLS-1$
	}

	@Override
	public void setDomain(String domain) {

	}

	@Override
	public String getDomain() {
		return ""; //$NON-NLS-1$
	}

	@Override
	public void setPath(String path) {

	}

	@Override
	public String getPath() {
		return ""; //$NON-NLS-1$
	}

	@Override
	public void setComment(String comment) {

	}

	@Override
	public String getComment() {
		return ""; //$NON-NLS-1$
	}

	@Override
	public void setHttpOnly(boolean httpOnly) {

	}

	@Override
	public boolean isHttpOnly() {
		return false;
	}

	@Override
	public void setSecure(boolean secure) {

	}

	@Override
	public boolean isSecure() {
		return false;
	}

	@Override
	public void setMaxAge(int maxAge) {
		
	}

	@Override
	public int getMaxAge() {
		return 0;
	}

	@Override
	public void setAttribute(String name, String value) {
		
	}

	@Override
	public String getAttribute(String name) {
		return null;
	}

	@Override
	public Map<String, String> getAttributes() {
		return Collections.emptyMap();
	}

}
