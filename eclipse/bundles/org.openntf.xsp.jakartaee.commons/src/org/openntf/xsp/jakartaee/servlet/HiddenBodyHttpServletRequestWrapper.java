package org.openntf.xsp.jakartaee.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletInputStream;

import com.ibm.commons.util.StringUtil;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This variant of {@link NewHttpServletRequestWrapper}
 * 
 * @author Jesse Gallagher
 * @since 2.10.0
 */
class HiddenBodyHttpServletRequestWrapper extends NewHttpServletRequestWrapper {

	public HiddenBodyHttpServletRequestWrapper(HttpServletRequest delegate) {
		super(delegate);
	}

	public HiddenBodyHttpServletRequestWrapper(ServletRequest delegate) {
		super(delegate);
	}
	
	@Override
	public ServletInputStream getInputStream() throws IOException {
		return new NOPServletInputStream();
	}
	
	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new StringReader("")); //$NON-NLS-1$
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Map getParameterMap() {
		return Collections.emptyMap();
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public Enumeration getParameterNames() {
		return Collections.emptyEnumeration();
	}
	
	@Override
	public String getParameter(String arg0) {
		return null;
	}
	
	@Override
	public String[] getParameterValues(String arg0) {
		return StringUtil.EMPTY_STRING_ARRAY;
	}

	private static class NOPServletInputStream extends ServletInputStream {

		@Override
		public int readLine(byte[] b, int off, int len) throws IOException {
			return -1;
		}

		@Override
		public int read() throws IOException {
			return -1;
		}

		@Override
		public int read(byte[] b) throws IOException {
			return -1;
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			return -1;
		}

		@Override
		public long skip(long n) throws IOException {
			return 0;
		}

		@Override
		public int available() throws IOException {
			return 0;
		}

		@Override
		public void close() throws IOException {
			super.close();
		}

		@Override
		public synchronized void mark(int readlimit) {
			super.mark(readlimit);
		}

		@Override
		public synchronized void reset() throws IOException {
			super.reset();
		}

		@Override
		public boolean markSupported() {
			return false;
		}
		
	}
}
