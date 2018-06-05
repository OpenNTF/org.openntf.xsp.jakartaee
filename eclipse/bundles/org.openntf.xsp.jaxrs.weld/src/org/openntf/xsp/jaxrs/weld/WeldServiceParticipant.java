package org.openntf.xsp.jaxrs.weld;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openntf.xsp.cdi.context.RequestContext;
import org.openntf.xsp.jaxrs.ServiceParticipant;

public class WeldServiceParticipant implements ServiceParticipant {

	@Override
	public void doBeforeService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RequestContext.inject();
	}

	@Override
	public void doAfterService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		RequestContext.eject();
	}

}
