package servlet;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class WebXmlServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String param = getInitParameter("initGuy");
		
		resp.setContentType("text/plain");
		try(PrintWriter w = resp.getWriter()) {
			w.print("I am the web.xml Servlet, and I was initialized with " + param);
		}
	}
}
