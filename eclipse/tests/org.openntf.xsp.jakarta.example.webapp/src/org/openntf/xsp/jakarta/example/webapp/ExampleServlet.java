package org.openntf.xsp.jakarta.example.webapp;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Jesse Gallagher
 * @since 2.8.0
 */
public class ExampleServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setContentType("text/plain");
		resp.getWriter().println("Hello from ExampleServlet. context=" + req.getContextPath() + ", path=" + req.getServletPath() + ", pathInfo=" + req.getPathInfo());
		resp.getWriter().flush();
	}

}
