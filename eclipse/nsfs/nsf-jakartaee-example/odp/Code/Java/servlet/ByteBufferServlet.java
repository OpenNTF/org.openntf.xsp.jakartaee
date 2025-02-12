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
