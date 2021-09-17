package org.openntf.xsp.jakartaee.servlet;

import java.io.IOException;

import jakarta.servlet.ServletInputStream;

public class NewServletInputStreamWrapper extends javax.servlet.ServletInputStream {
	private final ServletInputStream delegate;
	
	public NewServletInputStreamWrapper(ServletInputStream delegate) {
		this.delegate = delegate;
	}

	@Override
	public int read() throws IOException {
		return delegate.read();
	}
}
