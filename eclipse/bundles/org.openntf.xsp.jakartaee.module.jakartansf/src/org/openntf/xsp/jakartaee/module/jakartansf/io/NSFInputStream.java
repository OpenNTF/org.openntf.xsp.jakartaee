/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakartaee.module.jakartansf.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesSession;

public class NSFInputStream extends InputStream {
	private final NotesSession session;
	private final InputStream delegate;
	
	public NSFInputStream(NotesSession session, InputStream delegate) {
		this.session = session;
		this.delegate = delegate;
	}

	public void close() throws IOException {
		delegate.close();
		
		try {
			session.recycle();
		} catch (NotesAPIException e) {
			// Ignore
		}
	}

	@Override
	public int read() throws IOException {
		return this.delegate.read();
	}

	public int hashCode() {
		return delegate.hashCode();
	}

	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	public int read(byte[] b) throws IOException {
		return delegate.read(b);
	}

	public int read(byte[] b, int off, int len) throws IOException {
		return delegate.read(b, off, len);
	}

	public String toString() {
		return delegate.toString();
	}

	public byte[] readAllBytes() throws IOException {
		return delegate.readAllBytes();
	}

	public byte[] readNBytes(int len) throws IOException {
		return delegate.readNBytes(len);
	}

	public int readNBytes(byte[] b, int off, int len) throws IOException {
		return delegate.readNBytes(b, off, len);
	}

	public long skip(long n) throws IOException {
		return delegate.skip(n);
	}

	public void skipNBytes(long n) throws IOException {
		delegate.skipNBytes(n);
	}

	public int available() throws IOException {
		return delegate.available();
	}

	public void mark(int readlimit) {
		delegate.mark(readlimit);
	}

	public void reset() throws IOException {
		delegate.reset();
	}

	public boolean markSupported() {
		return delegate.markSupported();
	}

	public long transferTo(OutputStream out) throws IOException {
		return delegate.transferTo(out);
	}
}