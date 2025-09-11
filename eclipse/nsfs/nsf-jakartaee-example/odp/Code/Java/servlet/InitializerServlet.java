package servlet;

import java.io.IOException;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/initializerResource")
public class InitializerServlet extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ServletContext servletContext = req.getServletContext();
		resp.getWriter().print(String.valueOf(servletContext.getAttribute(ExampleServletContainerInitializer.class.getName())));
		resp.getWriter().flush();
	}
	
}
