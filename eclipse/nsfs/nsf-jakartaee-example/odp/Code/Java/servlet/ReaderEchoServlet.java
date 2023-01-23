package servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This servlet uses {@link HttpServletRequest#getReader()} to read POST
 * body data and echo it back as text to the caller.
 * 
 * @author Jesse Gallagher
 * @since 2.10.0
 */
@WebServlet(urlPatterns = { "/echoServlet" })
public class ReaderEchoServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/plain");
		
		BufferedReader r = req.getReader();
		try(PrintWriter w = resp.getWriter()) {
		
			String line;
			while((line = r.readLine()) != null) {
				w.write(line);
			}
		}
	}
}
