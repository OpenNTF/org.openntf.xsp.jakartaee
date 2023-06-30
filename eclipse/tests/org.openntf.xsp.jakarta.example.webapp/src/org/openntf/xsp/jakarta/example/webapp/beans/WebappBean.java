package org.openntf.xsp.jakarta.example.webapp.beans;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WebappBean {
	public String getHello() {
		return "Hello from webappBean";
	}
	
	public int getIdentity() {
		return System.identityHashCode(this);
	}
}
