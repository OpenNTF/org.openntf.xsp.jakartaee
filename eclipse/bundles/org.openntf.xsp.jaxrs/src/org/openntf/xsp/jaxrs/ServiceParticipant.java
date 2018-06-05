package org.openntf.xsp.jaxrs;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface ServiceParticipant {
	public static final String EXTENSION_POINT = ServiceParticipant.class.getName();
	
	void doBeforeService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
	void doAfterService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;
}
