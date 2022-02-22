package org.openntf.xsp.test.beanbundle.servlet.resources;

import org.openntf.xsp.test.beanbundle.BundleBean;

import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("bean")
public class BundleBeanResource {
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Object get() {
		return CDI.current().select(BundleBean.class).get();
	}
}
