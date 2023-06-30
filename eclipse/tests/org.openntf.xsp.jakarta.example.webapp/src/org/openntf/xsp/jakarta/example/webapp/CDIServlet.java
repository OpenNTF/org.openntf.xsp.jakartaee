package org.openntf.xsp.jakarta.example.webapp;

import java.io.IOException;

import org.openntf.xsp.jakarta.example.webapp.beans.WebappBean;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CDIServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		WebappBean bean = CDI.current().select(WebappBean.class).get();
		ServletOutputStream out = resp.getOutputStream();
		Jsonb jsonb = JsonbBuilder.create();
		jsonb.toJson(bean, out);
	}
}
