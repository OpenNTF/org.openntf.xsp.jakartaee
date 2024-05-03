package org.openntf.xsp.cdi.bean;

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
	
	public void addCookie(Cookie cookie) {
		delegate().addCookie(cookie);
	}

	public boolean containsHeader(String name) {
		return delegate().containsHeader(name);
	}

	public String encodeURL(String url) {
		return delegate().encodeURL(url);
	}

	public String getCharacterEncoding() {
		return delegate().getCharacterEncoding();
	}

	public String encodeRedirectURL(String url) {
		return delegate().encodeRedirectURL(url);
	}

	public void sendError(int sc, String msg) throws IOException {
		delegate().sendError(sc, msg);
	}

	public String getContentType() {
		return delegate().getContentType();
	}

	public ServletOutputStream getOutputStream() throws IOException {
		return delegate().getOutputStream();
	}

	public void sendError(int sc) throws IOException {
		delegate().sendError(sc);
	}

	public PrintWriter getWriter() throws IOException {
		return delegate().getWriter();
	}

	public void sendRedirect(String location) throws IOException {
		delegate().sendRedirect(location);
	}

	public void setCharacterEncoding(String charset) {
		delegate().setCharacterEncoding(charset);
	}

	public void setDateHeader(String name, long date) {
		delegate().setDateHeader(name, date);
	}

	public void addDateHeader(String name, long date) {
		delegate().addDateHeader(name, date);
	}

	public void setHeader(String name, String value) {
		delegate().setHeader(name, value);
	}

	public void addHeader(String name, String value) {
		delegate().addHeader(name, value);
	}

	public void setContentLength(int len) {
		delegate().setContentLength(len);
	}

	public void setIntHeader(String name, int value) {
		delegate().setIntHeader(name, value);
	}

	public void setContentLengthLong(long len) {
		delegate().setContentLengthLong(len);
	}

	public void setContentType(String type) {
		delegate().setContentType(type);
	}

	public void addIntHeader(String name, int value) {
		delegate().addIntHeader(name, value);
	}

	public void setStatus(int sc) {
		delegate().setStatus(sc);
	}

	public int getStatus() {
		return delegate().getStatus();
	}

	public String getHeader(String name) {
		return delegate().getHeader(name);
	}

	public void setBufferSize(int size) {
		delegate().setBufferSize(size);
	}

	public Collection<String> getHeaders(String name) {
		return delegate().getHeaders(name);
	}

	public Collection<String> getHeaderNames() {
		return delegate().getHeaderNames();
	}

	public int getBufferSize() {
		return delegate().getBufferSize();
	}

	public void flushBuffer() throws IOException {
		delegate().flushBuffer();
	}

	public void setTrailerFields(Supplier<Map<String, String>> supplier) {
		delegate().setTrailerFields(supplier);
	}

	public void resetBuffer() {
		delegate().resetBuffer();
	}

	public boolean isCommitted() {
		return delegate().isCommitted();
	}

	public void reset() {
		delegate().reset();
	}

	public Supplier<Map<String, String>> getTrailerFields() {
		return delegate().getTrailerFields();
	}

	public void setLocale(Locale loc) {
		delegate().setLocale(loc);
	}

	public Locale getLocale() {
		return delegate().getLocale();
	}

	private HttpServletResponse delegate() {
		return HttpContextBean.THREAD_RESPONSES.get();
	}
}
