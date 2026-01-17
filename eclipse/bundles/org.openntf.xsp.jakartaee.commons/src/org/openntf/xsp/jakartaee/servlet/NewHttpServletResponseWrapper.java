/**
 * Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
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
import java.util.Locale;

import jakarta.servlet.http.HttpServletResponse;

class NewHttpServletResponseWrapper implements javax.servlet.http.HttpServletResponse {
	final HttpServletResponse delegate;

	public NewHttpServletResponseWrapper(final HttpServletResponse delegate) {
		this.delegate = delegate;
	}

	@Override
	public void flushBuffer() throws IOException {
		this.delegate.flushBuffer();
	}

	@Override
	public int getBufferSize() {
		return delegate.getBufferSize();
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
	public Locale getLocale() {
		return delegate.getLocale();
	}

	@Override
	public javax.servlet.ServletOutputStream getOutputStream() throws IOException {
		return ServletUtil.newToOld(delegate.getOutputStream());
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return delegate.getWriter();
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
	public void resetBuffer() {
		delegate.resetBuffer();
	}

	@Override
	public void setBufferSize(final int arg0) {
		delegate.setBufferSize(arg0);
	}

	@Override
	public void setCharacterEncoding(final String arg0) {
		delegate.setCharacterEncoding(arg0);
	}

	@Override
	public void setContentLength(final int arg0) {
		delegate.setContentLength(arg0);
	}

	@Override
	public void setContentType(final String arg0) {
		delegate.setContentType(arg0);
	}

	@Override
	public void setLocale(final Locale arg0) {
		delegate.setLocale(arg0);
	}

	@Override
	public void addCookie(final javax.servlet.http.Cookie arg0) {
		delegate.addCookie(ServletUtil.oldToNew(arg0));
	}

	@Override
	public void addDateHeader(final String arg0, final long arg1) {
		delegate.addDateHeader(arg0, arg1);
	}

	@Override
	public void addHeader(final String arg0, final String arg1) {
		delegate.addHeader(arg0, arg1);
	}

	@Override
	public void addIntHeader(final String arg0, final int arg1) {
		delegate.addIntHeader(arg0, arg1);
	}

	@Override
	public boolean containsHeader(final String arg0) {
		return delegate.containsHeader(arg0);
	}

	@Override
	public String encodeRedirectURL(final String arg0) {
		return delegate.encodeRedirectURL(arg0);
	}

	@Override
	public String encodeRedirectUrl(final String arg0) {
		// Duplicate removed in Servlet 6
		return delegate.encodeRedirectURL(arg0);
	}

	@Override
	public String encodeURL(final String arg0) {
		return delegate.encodeURL(arg0);
	}

	@Override
	public String encodeUrl(final String arg0) {
		// Duplicate removed in Servlet 6
		return delegate.encodeURL(arg0);
	}

	@Override
	public void sendError(final int arg0) throws IOException {
		delegate.sendError(arg0);
	}

	@Override
	public void sendError(final int arg0, final String arg1) throws IOException {
		delegate.sendError(arg0, arg1);
	}

	@Override
	public void sendRedirect(final String arg0) throws IOException {
		delegate.sendRedirect(arg0);
	}

	@Override
	public void setDateHeader(final String arg0, final long arg1) {
		delegate.setDateHeader(arg0, arg1);
	}

	@Override
	public void setHeader(final String arg0, final String arg1) {
		delegate.setHeader(arg0, arg1);
	}

	@Override
	public void setIntHeader(final String arg0, final int arg1) {
		delegate.setIntHeader(arg0, arg1);
	}

	@Override
	public void setStatus(final int arg0) {
		delegate.setStatus(arg0);
	}

	@Override
	public void setStatus(final int arg0, final String arg1) {
		// Removed in Servlet 6
		delegate.setStatus(arg0);
	}

}
