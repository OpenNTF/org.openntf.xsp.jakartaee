package org.openntf.xsp.test.beanbundle.servlet;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.openntf.xsp.test.beanbundle.servlet.resources.RootResource;

import jakarta.ws.rs.core.Application;

public class ExampleApplication extends Application {
	@Override
	public Set<Class<?>> getClasses() {
		return new HashSet<>(Arrays.asList(
			RootResource.class
		));
	}
}
