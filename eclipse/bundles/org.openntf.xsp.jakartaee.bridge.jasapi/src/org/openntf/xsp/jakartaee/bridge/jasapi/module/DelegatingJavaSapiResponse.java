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
package org.openntf.xsp.jakartaee.bridge.jasapi.module;

import java.io.IOException;
import java.io.PrintWriter;

import com.ibm.domino.bridge.http.jasapi.IJavaSapiHttpResponseAdapter;

import org.openntf.xsp.jakartaee.jasapi.JavaSapiResponse;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;

import jakarta.servlet.ServletOutputStream;

/**
 *
 * @author Jesse Gallagher
 * @since 2.13.0
 */
public class DelegatingJavaSapiResponse implements JavaSapiResponse {
	private final IJavaSapiHttpResponseAdapter delegate;

	public DelegatingJavaSapiResponse(final IJavaSapiHttpResponseAdapter delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean containsHeader(final String arg0) {
		return delegate.containsHeader(arg0);
	}

	@Override
	public void flushBuffer() throws IOException {
		delegate.flushBuffer();
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
	public ServletOutputStream getOutputStream() {
		return ServletUtil.oldToNew(delegate.getOutputStream());
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
	public boolean outputStreamInUse() {
		return delegate.outputStreamInUse();
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
	public void setBufferSize(final int arg0) {
		delegate.setBufferSize(arg0);
	}

	@Override
	public void setCharacterEncoding(final String arg0) {
		delegate.setCharacterEncoding(arg0);
	}

	@Override
	public void setHeader(final String arg0, final String arg1) {
		delegate.setHeader(arg0, arg1);
	}

	@Override
	public void setHeader(final String arg0) {
		delegate.setHeader(arg0);
	}

	@Override
	public void setIntHeader(final String arg0, final int arg1) {
		delegate.setIntHeader(arg0, arg1);
	}

	@Override
	public void setStatus(final int arg0, final String arg1) {
		delegate.setStatus(arg0, arg1);
	}

	@Override
	public void setStatus(final int arg0) {
		delegate.setStatus(arg0);
	}

	@Override
	public void write(final byte[] arg0, final int arg1, final int arg2) throws IOException {
		delegate.write(arg0, arg1, arg2);
	}

	@Override
	public void writeHeaders() {
		delegate.writeHeaders();
	}

	@Override
	public boolean writerInUse() {
		return delegate.writerInUse();
	}



}
