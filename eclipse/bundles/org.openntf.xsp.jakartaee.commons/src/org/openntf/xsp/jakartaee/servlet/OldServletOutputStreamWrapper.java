package org.openntf.xsp.jakartaee.servlet;

import java.io.IOException;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;

public class OldServletOutputStreamWrapper extends ServletOutputStream {
	private final javax.servlet.ServletOutputStream delegate;
	
	public OldServletOutputStreamWrapper(javax.servlet.ServletOutputStream delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public boolean isReady() {
		return true;
	}

	@Override
	public void setWriteListener(WriteListener writeListener) {
		// Soft unavailable
	}

	@Override
	public void write(int b) throws IOException {
		delegate.write(b);
	}
}
