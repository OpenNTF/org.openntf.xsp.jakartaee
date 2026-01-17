/**
 * Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakartaee.jasapi;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletOutputStream;

/**
 * Response object for an incoming JavaSapi event.
 *
 * @author Jesse Gallagher
 * @since 2.13.0
 */
public interface JavaSapiResponse {
	String getCharacterEncoding();

	void setCharacterEncoding(String encoding);

	boolean writerInUse();

	boolean outputStreamInUse();

	void setStatus(int code, String reason);

	void setStatus(int code);

	void setHeader(String headerName, String value);

	void setHeader(String header);

	boolean containsHeader(String headerName);

	void setIntHeader(String headerName, int value);

	ServletOutputStream getOutputStream();

	PrintWriter getWriter() throws IOException;

	void writeHeaders();

	void write(byte b[], int off, int len) throws IOException;

	int getBufferSize();

	void setBufferSize(int bufferSize);

	void flushBuffer() throws IOException;

	boolean isCommitted();

	void reset();

	void resetBuffer();
}
