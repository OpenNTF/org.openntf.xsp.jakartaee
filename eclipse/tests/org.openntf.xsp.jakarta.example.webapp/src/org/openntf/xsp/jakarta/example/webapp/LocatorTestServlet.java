package org.openntf.xsp.jakarta.example.webapp;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LocatorTestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try(PrintWriter w = resp.getWriter()) {
			try {
				w.println("I am going to try to locate context information");
				
				w.println("Module: " +
					LibraryUtil.findExtensionsSorted(ComponentModuleLocator.class, false)
						.stream()
						.map(ComponentModuleLocator::findActiveModule)
						.filter(Optional::isPresent)
						.findFirst()
						.map(Optional::get)
				);
				
				Optional<HttpServletRequest> request = LibraryUtil.findExtensionsSorted(ComponentModuleLocator.class, false)
					.stream()
					.map(ComponentModuleLocator::findServletRequest)
					.filter(Optional::isPresent)
					.findFirst()
					.map(Optional::get)
					.map(HttpServletRequest.class::cast);
				w.println("Request: " + request);
				
				
				w.println("Context: " +
					LibraryUtil.findExtensionsSorted(ComponentModuleLocator.class, false)
						.stream()
						.map(ComponentModuleLocator::findServletContext)
						.filter(Optional::isPresent)
						.findFirst()
						.map(Optional::get)
				);
			} catch(Exception e) {
				e.printStackTrace(w);
			}
		}
	}

}
