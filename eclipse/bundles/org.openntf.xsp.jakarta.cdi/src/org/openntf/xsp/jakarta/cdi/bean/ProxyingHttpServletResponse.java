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
package org.openntf.xsp.jakarta.cdi.bean;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @since 3.0.0
 */
public class ProxyingHttpServletResponse implements HttpServletResponse {
	public static HttpServletResponse INSTANCE = new ProxyingHttpServletResponse();

	@Override
	public void addCookie(final Cookie cookie) {
		delegate().addCookie(cookie);
	}

	@Override
	public boolean containsHeader(final String name) {
		return delegate().containsHeader(name);
	}

	@Override
	public String encodeURL(final String url) {
		return delegate().encodeURL(url);
	}

	@Override
	public String getCharacterEncoding() {
		return delegate().getCharacterEncoding();
	}

	@Override
	public String encodeRedirectURL(final String url) {
		return delegate().encodeRedirectURL(url);
	}

	@Override
	public void sendError(final int sc, final String msg) throws IOException {
		delegate().sendError(sc, msg);
	}

	@Override
	public String getContentType() {
		return delegate().getContentType();
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return delegate().getOutputStream();
	}

	@Override
	public void sendError(final int sc) throws IOException {
		delegate().sendError(sc);
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return delegate().getWriter();
	}

	@Override
	public void sendRedirect(final String location) throws IOException {
		delegate().sendRedirect(location);
	}

	@Override
	public void setCharacterEncoding(final String charset) {
		delegate().setCharacterEncoding(charset);
	}

	@Override
	public void setDateHeader(final String name, final long date) {
		delegate().setDateHeader(name, date);
	}

	@Override
	public void addDateHeader(final String name, final long date) {
		delegate().addDateHeader(name, date);
	}

	@Override
	public void setHeader(final String name, final String value) {
		delegate().setHeader(name, value);
	}

	@Override
	public void addHeader(final String name, final String value) {
		delegate().addHeader(name, value);
	}

	@Override
	public void setContentLength(final int len) {
		delegate().setContentLength(len);
	}

	@Override
	public void setIntHeader(final String name, final int value) {
		delegate().setIntHeader(name, value);
	}

	@Override
	public void setContentLengthLong(final long len) {
		delegate().setContentLengthLong(len);
	}

	@Override
	public void setContentType(final String type) {
		delegate().setContentType(type);
	}

	@Override
	public void addIntHeader(final String name, final int value) {
		delegate().addIntHeader(name, value);
	}

	@Override
	public void setStatus(final int sc) {
		delegate().setStatus(sc);
	}

	@Override
	public int getStatus() {
		return delegate().getStatus();
	}

	@Override
	public String getHeader(final String name) {
		return delegate().getHeader(name);
	}

	@Override
	public void setBufferSize(final int size) {
		delegate().setBufferSize(size);
	}

	@Override
	public Collection<String> getHeaders(final String name) {
		return delegate().getHeaders(name);
	}

	@Override
	public Collection<String> getHeaderNames() {
		return delegate().getHeaderNames();
	}

	@Override
	public int getBufferSize() {
		return delegate().getBufferSize();
	}

	@Override
	public void flushBuffer() throws IOException {
		delegate().flushBuffer();
	}

	@Override
	public void setTrailerFields(final Supplier<Map<String, String>> supplier) {
		delegate().setTrailerFields(supplier);
	}

	@Override
	public void resetBuffer() {
		delegate().resetBuffer();
	}

	@Override
	public boolean isCommitted() {
		return delegate().isCommitted();
	}

	@Override
	public void reset() {
		delegate().reset();
	}

	@Override
	public Supplier<Map<String, String>> getTrailerFields() {
		return delegate().getTrailerFields();
	}

	@Override
	public void setLocale(final Locale loc) {
		delegate().setLocale(loc);
	}

	@Override
	public Locale getLocale() {
		return delegate().getLocale();
	}

	private HttpServletResponse delegate() {
		return HttpContextBean.THREAD_RESPONSES.get();
	}
}
