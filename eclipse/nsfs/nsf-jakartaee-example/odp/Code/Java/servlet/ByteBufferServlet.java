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
package servlet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = "/byteBufferServlet")
public class ByteBufferServlet extends HttpServlet {
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ServletInputStream is = req.getInputStream();
		
		ByteBuffer buf = ByteBuffer.allocate(1024);
		int read = is.read(buf);
		
		buf.position(0);
		byte[] readData = new byte[read];
		buf.get(0, readData);
		
		String result = "Read " + read + " bytes of data: " + new String(readData);
		
		resp.setContentType("text/plain");
		resp.setCharacterEncoding(StandardCharsets.US_ASCII);
		ServletOutputStream out = resp.getOutputStream();
		ByteBuffer outBuf = ByteBuffer.wrap(result.getBytes(StandardCharsets.US_ASCII));
		out.write(outBuf);
	}
}
