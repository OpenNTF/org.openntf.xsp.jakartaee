package org.openntf.xsp.jakarta.example.webapp;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SuppressWarnings("nls")
public class LocatorTestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try(PrintWriter w = resp.getWriter()) {
			try {
				w.println("I am going to try to locate context information");
				
				w.println("Active locators:");
				LibraryUtil.findExtensionsSorted(ComponentModuleLocator.class, false)
					.stream()
					.filter(ComponentModuleLocator::isActive)
					.forEach(loc -> w.println(">> " + loc));
				w.println();
				
				ComponentModuleLocator loc = ComponentModuleLocator.getDefault().get();
				w.println("Module: " + loc.getActiveModule());
				
				Optional<HttpServletRequest> request = loc.getServletRequest();
				w.println("Request: " + request);
				if(request.isPresent()) {
					w.println("- Context path: " + request.get().getContextPath());
					w.println("- Servlet path: " + request.get().getServletPath());
					w.println("- URI: " + request.get().getRequestURI());
					w.println("- URL: " + request.get().getRequestURL());
				}
				
				Optional<ServletContext> context = loc.getServletContext();
				w.println("Context: " + context);
				if(context.isPresent()) {
					w.println("- Context path: " + context.get().getContextPath());
				}
				
			} catch(Exception e) {
				e.printStackTrace(w);
			}
		}
	}

}
