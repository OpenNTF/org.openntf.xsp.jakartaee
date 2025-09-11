package servlet;

import java.io.IOException;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This Servlet is used to ensure that the initializer from the other NSF
 * does not contaminate this one.
 */
@SuppressWarnings("nls")
@WebServlet("/initializerResource")
public class InitializerServlet extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ServletContext servletContext = req.getServletContext();
		resp.getWriter().print(String.valueOf(servletContext.getAttribute("servlet.ExampleServletContainerInitializer")));
		resp.getWriter().flush();
	}
	
}
