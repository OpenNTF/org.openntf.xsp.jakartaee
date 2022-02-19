/**
 * Copyright © 2018-2022 Jesse Gallagher
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

class NewServletOutputStreamWrapper extends javax.servlet.ServletOutputStream {
	final ServletOutputStream delegate;
	
	public NewServletOutputStreamWrapper(ServletOutputStream delegate) {
		this.delegate = delegate;
	}

	@Override
	public void flush() throws IOException {
		delegate.flush();
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}

	@Override
	public void print(boolean b) throws IOException {
		delegate.print(b);
	}

	@Override
	public void print(char c) throws IOException {
		delegate.print(c);
	}

	@Override
	public void print(double d) throws IOException {
		delegate.print(d);
	}

	@Override
	public void print(float f) throws IOException {
		delegate.print(f);
	}

	@Override
	public void print(int i) throws IOException {
		delegate.print(i);
	}

	@Override
	public void print(long l) throws IOException {
		delegate.print(l);
	}

	@Override
	public void print(String arg0) throws IOException {
		delegate.print(arg0);
	}

	@Override
	public void println() throws IOException {
		delegate.println();
	}

	@Override
	public void println(boolean b) throws IOException {
		delegate.println(b);
	}

	@Override
	public void println(char c) throws IOException {
		delegate.println(c);
	}

	@Override
	public void println(double d) throws IOException {
		delegate.println(d);
	}

	@Override
	public void println(float f) throws IOException {
		delegate.println(f);
	}

	@Override
	public void println(int i) throws IOException {
		delegate.println(i);
	}

	@Override
	public void println(long l) throws IOException {
		delegate.println(l);
	}

	@Override
	public void println(String s) throws IOException {
		delegate.println(s);
	}

	@Override
	public void write(int b) throws IOException {
		delegate.write(b);
	}

	@Override
	public void write(byte[] b) throws IOException {
		delegate.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		delegate.write(b, off, len);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}
}
