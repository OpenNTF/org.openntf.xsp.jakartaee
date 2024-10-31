/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
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

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;

class OldServletOutputStreamWrapper extends ServletOutputStream {
	final javax.servlet.ServletOutputStream delegate;

	public OldServletOutputStreamWrapper(final javax.servlet.ServletOutputStream delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean isReady() {
		return true;
	}

	@Override
	public void setWriteListener(final WriteListener writeListener) {
		// Soft unavailable
	}

	@Override
	public void write(final int b) throws IOException {
		delegate.write(b);
	}

	@Override
	public void print(final String s) throws IOException {
		delegate.print(s);
	}

	@Override
	public void print(final boolean b) throws IOException {
		delegate.print(b);
	}

	@Override
	public void print(final char c) throws IOException {
		delegate.print(c);
	}

	@Override
	public void write(final byte[] b) throws IOException {
		delegate.write(b);
	}

	@Override
	public void print(final int i) throws IOException {
		delegate.print(i);
	}

	@Override
	public void print(final long l) throws IOException {
		delegate.print(l);
	}

	@Override
	public void write(final byte[] b, final int off, final int len) throws IOException {
		delegate.write(b, off, len);
	}

	@Override
	public void print(final float f) throws IOException {
		delegate.print(f);
	}

	@Override
	public void print(final double d) throws IOException {
		delegate.print(d);
	}

	@Override
	public void println() throws IOException {
		delegate.println();
	}

	@Override
	public void println(final String s) throws IOException {
		delegate.println(s);
	}

	@Override
	public void println(final boolean b) throws IOException {
		delegate.println(b);
	}

	@Override
	public void println(final char c) throws IOException {
		delegate.println(c);
	}

	@Override
	public void println(final int i) throws IOException {
		delegate.println(i);
	}

	@Override
	public void flush() throws IOException {
		delegate.flush();
	}

	@Override
	public void println(final long l) throws IOException {
		delegate.println(l);
	}

	@Override
	public void println(final float f) throws IOException {
		delegate.println(f);
	}

	@Override
	public void println(final double d) throws IOException {
		delegate.println(d);
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}

	@Override
	public String toString() {
		return delegate.toString();
	}
}
