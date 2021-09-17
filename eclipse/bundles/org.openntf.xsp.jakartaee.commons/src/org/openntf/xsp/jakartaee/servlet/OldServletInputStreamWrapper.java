package org.openntf.xsp.jakartaee.servlet;

import java.io.IOException;
import java.io.UncheckedIOException;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

public class OldServletInputStreamWrapper extends ServletInputStream {
	private final javax.servlet.ServletInputStream delegate;
	
	public OldServletInputStreamWrapper(javax.servlet.ServletInputStream delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean isFinished() {
		try {
			return delegate.available() == 0;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public boolean isReady() {
		return true;
	}

	@Override
	public void setReadListener(ReadListener readListener) {
		// NOP, hopefully
	}

	@Override
	public int read() throws IOException {
		return delegate.read();
	}

}
