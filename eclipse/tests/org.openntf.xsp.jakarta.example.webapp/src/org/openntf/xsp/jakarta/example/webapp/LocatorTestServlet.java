package org.openntf.xsp.jakarta.example.webapp;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;

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
				
				ComponentModuleLocator loc = ComponentModuleLocator.getDefault().get();
				w.println("Module: " + loc.getActiveModule());
				
				Optional<HttpServletRequest> request = loc.getServletRequest();
				w.println("Request: " + request);
				
				
				w.println("Context: " + loc.getServletContext());
			} catch(Exception e) {
				e.printStackTrace(w);
			}
		}
	}

}
