/**
 * Copyright © 2018-2021 Jesse Gallagher
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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

class OldHttpServletResponseWrapper implements HttpServletResponse {
	final javax.servlet.http.HttpServletResponse delegate;
	
	public OldHttpServletResponseWrapper(javax.servlet.http.HttpServletResponse delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public String getCharacterEncoding() {
		return delegate.getCharacterEncoding();
	}

	@Override
	public String getContentType() {
		return delegate.getContentType();
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return ServletUtil.oldToNew(delegate.getOutputStream());
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return delegate.getWriter();
	}

	@Override
	public void setCharacterEncoding(String charset) {
		delegate.setCharacterEncoding(charset);
	}

	@Override
	public void setContentLength(int len) {
		delegate.setContentLength(len);
	}

	@Override
	public void setContentLengthLong(long len) {
		if(len > Integer.MAX_VALUE) {
			throw new UnsupportedOperationException("Cannot set length larger than INT_MAX");
		}
		delegate.setContentLength((int)len);
	}

	@Override
	public void setContentType(String type) {
		delegate.setContentType(type);
	}

	@Override
	public void setBufferSize(int size) {
		delegate.setBufferSize(size);
	}

	@Override
	public int getBufferSize() {
		return delegate.getBufferSize();
	}

	@Override
	public void flushBuffer() throws IOException {
		delegate.flushBuffer();
	}

	@Override
	public void resetBuffer() {
		delegate.resetBuffer();
	}

	@Override
	public boolean isCommitted() {
		return delegate.isCommitted();
	}

	@Override
	public void reset() {
		delegate.reset();
	}

	@Override
	public void setLocale(Locale loc) {
		delegate.setLocale(loc);
	}

	@Override
	public Locale getLocale() {
		return delegate.getLocale();
	}

	@Override
	public void addCookie(Cookie cookie) {
		delegate.addCookie(ServletUtil.newToOld(cookie));
	}

	@Override
	public boolean containsHeader(String name) {
		return delegate.containsHeader(name);
	}

	@Override
	public String encodeURL(String url) {
		return delegate.encodeURL(url);
	}

	@Override
	public String encodeRedirectURL(String url) {
		return delegate.encodeRedirectURL(url);
	}

	@SuppressWarnings("deprecation")
	@Override
	public String encodeUrl(String url) {
		return delegate.encodeUrl(url);
	}

	@SuppressWarnings("deprecation")
	@Override
	public String encodeRedirectUrl(String url) {
		return delegate.encodeRedirectUrl(url);
	}

	@Override
	public void sendError(int sc, String msg) throws IOException {
		delegate.sendError(sc, msg);
	}

	@Override
	public void sendError(int sc) throws IOException {
		delegate.sendError(sc);
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		delegate.sendRedirect(location);
	}

	@Override
	public void setDateHeader(String name, long date) {
		delegate.setDateHeader(name, date);
	}

	@Override
	public void addDateHeader(String name, long date) {
		delegate.addDateHeader(name, date);
	}

	@Override
	public void setHeader(String name, String value) {
		delegate.setHeader(name, value);
	}

	@Override
	public void addHeader(String name, String value) {
		delegate.addHeader(name, value);
	}

	@Override
	public void setIntHeader(String name, int value) {
		delegate.setIntHeader(name, value);
	}

	@Override
	public void addIntHeader(String name, int value) {
		delegate.addIntHeader(name, value);
	}
	
	int status = 200;

	@Override
	public void setStatus(int sc) {
		delegate.setStatus(sc);
		this.status = sc;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setStatus(int sc, String sm) {
		delegate.setStatus(sc, sm);
		this.status = sc;
	}

	@Override
	public int getStatus() {
		return status;
	}

	@Override
	public String getHeader(String name) {
		// Soft unavailable
		return null;
	}

	@Override
	public Collection<String> getHeaders(String name) {
		// Soft unavailable
		return Collections.emptySet();
	}

	@Override
	public Collection<String> getHeaderNames() {
		// Soft unavailable
		return Collections.emptySet();
	}

}
