package org.openntf.xsp.jakarta.security.jasapi;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

import org.openntf.xsp.jakartaee.jasapi.JavaSapiResponse;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

public class JavaSapiHttpServletResponse implements HttpServletResponse {
	private final JavaSapiResponse delegate;
	
	public JavaSapiHttpServletResponse(JavaSapiResponse delegate) {
		this.delegate = delegate;
	}
	

	@Override
	public String getCharacterEncoding() {
		return delegate.getCharacterEncoding();
	}

	@Override
	public String getContentType() {
		return null;
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return delegate.getOutputStream();
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
		// NOP
	}

	@Override
	public void setContentLengthLong(long len) {
		// NOP
	}

	@Override
	public void setContentType(String type) {
		// NOP
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
		// NOP
	}

	@Override
	public Locale getLocale() {
		return null;
	}

	@Override
	public void addCookie(Cookie cookie) {
		// NOP
	}

	@Override
	public boolean containsHeader(String name) {
		return delegate.containsHeader(name);
	}

	@Override
	public String encodeURL(String url) {
		// NOP
		return url;
	}

	@Override
	public String encodeRedirectURL(String url) {
		// NOP
		return url;
	}

	@Override
	public void sendError(int sc, String msg) throws IOException {
		// NOP
	}

	@Override
	public void sendError(int sc) throws IOException {
		// NOP
	}

	@Override
	public void sendRedirect(String location) throws IOException {
		// NOP
	}

	@Override
	public void setDateHeader(String name, long date) {
		setHeader(name, new java.util.Date(date).toString());
	}

	@Override
	public void addDateHeader(String name, long date) {
		addHeader(name, new java.util.Date(date).toString());
	}

	@Override
	public void setHeader(String name, String value) {
		delegate.setHeader(name, value);
	}

	@Override
	public void addHeader(String name, String value) {
		// Soft workaround
		delegate.setHeader(name, value);
	}

	@Override
	public void setIntHeader(String name, int value) {
		setHeader(name, Integer.toString(value));
	}

	@Override
	public void addIntHeader(String name, int value) {
		addHeader(name, Integer.toString(value));
	}

	@Override
	public void setStatus(int sc) {
		delegate.setStatus(sc);
	}

	@Override
	public int getStatus() {
		return 0;
	}

	@Override
	public String getHeader(String name) {
		return null;
	}

	@Override
	public Collection<String> getHeaders(String name) {
		return null;
	}

	@Override
	public Collection<String> getHeaderNames() {
		return null;
	}


	@Override
	public void sendRedirect(String location, int sc, boolean clearBuffer) throws IOException {
		// NOP
	}

}