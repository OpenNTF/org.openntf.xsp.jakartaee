package org.openntf.xsp.jakartaee.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

public class NewHttpServletResponseWrapper implements javax.servlet.http.HttpServletResponse {
	private final HttpServletResponse delegate;
	
	public NewHttpServletResponseWrapper(HttpServletResponse delegate) {
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
		return new NewServletOutputStreamWrapper(delegate.getOutputStream());
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
	public void setBufferSize(int arg0) {
		delegate.setBufferSize(arg0);
	}

	@Override
	public void setCharacterEncoding(String arg0) {
		delegate.setCharacterEncoding(arg0);
	}

	@Override
	public void setContentLength(int arg0) {
		delegate.setContentLength(arg0);
	}

	@Override
	public void setContentType(String arg0) {
		delegate.setContentType(arg0);
	}

	@Override
	public void setLocale(Locale arg0) {
		delegate.setLocale(arg0);
	}

	@Override
	public void addCookie(javax.servlet.http.Cookie arg0) {
		delegate.addCookie(new Cookie(arg0.getName(), arg0.getValue()));
	}

	@Override
	public void addDateHeader(String arg0, long arg1) {
		delegate.addDateHeader(arg0, arg1);
	}

	@Override
	public void addHeader(String arg0, String arg1) {
		delegate.addHeader(arg0, arg1);
	}

	@Override
	public void addIntHeader(String arg0, int arg1) {
		delegate.addIntHeader(arg0, arg1);
	}

	@Override
	public boolean containsHeader(String arg0) {
		return delegate.containsHeader(arg0);
	}

	@Override
	public String encodeRedirectURL(String arg0) {
		return delegate.encodeRedirectURL(arg0);
	}

	@SuppressWarnings("deprecation")
	@Override
	public String encodeRedirectUrl(String arg0) {
		return delegate.encodeRedirectUrl(arg0);
	}

	@Override
	public String encodeURL(String arg0) {
		return delegate.encodeURL(arg0);
	}

	@SuppressWarnings("deprecation")
	@Override
	public String encodeUrl(String arg0) {
		return delegate.encodeUrl(arg0);
	}

	@Override
	public void sendError(int arg0) throws IOException {
		delegate.sendError(arg0);
	}

	@Override
	public void sendError(int arg0, String arg1) throws IOException {
		delegate.sendError(arg0, arg1);
	}

	@Override
	public void sendRedirect(String arg0) throws IOException {
		delegate.sendRedirect(arg0);
	}

	@Override
	public void setDateHeader(String arg0, long arg1) {
		delegate.setDateHeader(arg0, arg1);
	}

	@Override
	public void setHeader(String arg0, String arg1) {
		delegate.setHeader(arg0, arg1);
	}

	@Override
	public void setIntHeader(String arg0, int arg1) {
		delegate.setIntHeader(arg0, arg1);
	}

	@Override
	public void setStatus(int arg0) {
		delegate.setStatus(arg0);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setStatus(int arg0, String arg1) {
		delegate.setStatus(arg0, arg1);
	}

}
