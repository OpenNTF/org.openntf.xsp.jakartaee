package org.openntf.xsp.jakartaee.servlet;

import java.io.IOException;

import jakarta.servlet.ServletOutputStream;

public class NewServletOutputStreamWrapper extends javax.servlet.ServletOutputStream {
	private final ServletOutputStream delegate;
	
	public NewServletOutputStreamWrapper(ServletOutputStream delegate) {
		this.delegate = delegate;
	}

	@Override
	public void write(int b) throws IOException {
		delegate.write(b);
	}
}
