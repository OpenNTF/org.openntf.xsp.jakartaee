package org.openntf.xsp.test.beanbundle;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@ApplicationScoped
@Named("bundleBean")
public class BundleBean {
	public String getHello() {
		return "Hello from bundleBean";
	}
}
