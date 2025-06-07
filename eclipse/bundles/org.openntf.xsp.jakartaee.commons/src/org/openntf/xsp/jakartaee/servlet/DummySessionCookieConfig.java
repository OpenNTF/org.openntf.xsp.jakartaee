/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	public void setName(final String name) {

	}

	@Override
	public String getName() {
		return "JSESSIONID"; //$NON-NLS-1$
	}

	@Override
	public void setDomain(final String domain) {

	}

	@Override
	public String getDomain() {
		return ""; //$NON-NLS-1$
	}

	@Override
	public void setPath(final String path) {

	}

	@Override
	public String getPath() {
		return ""; //$NON-NLS-1$
	}

	@Override
	public void setComment(final String comment) {

	}

	@Override
	public String getComment() {
		return ""; //$NON-NLS-1$
	}

	@Override
	public void setHttpOnly(final boolean httpOnly) {

	}

	@Override
	public boolean isHttpOnly() {
		return false;
	}

	@Override
	public void setSecure(final boolean secure) {

	}

	@Override
	public boolean isSecure() {
		return false;
	}

	@Override
	public void setMaxAge(final int maxAge) {

	}

	@Override
	public int getMaxAge() {
		return 0;
	}

	@Override
	public void setAttribute(final String name, final String value) {

	}

	@Override
	public String getAttribute(final String name) {
		return null;
	}

	@Override
	public Map<String, String> getAttributes() {
		return Collections.emptyMap();
	}

}
