package org.openntf.xsp.mvc.impl;

import java.util.Set;

import org.eclipse.krazo.servlet.KrazoContainerInitializer;
import org.openntf.xsp.jakartaee.LibraryUtil;
import org.openntf.xsp.mvc.MvcLibrary;
import com.ibm.xsp.application.ApplicationEx;

import jakarta.servlet.ServletContext;
import jakarta.servlet.annotation.HandlesTypes;
import jakarta.ws.rs.Path;

@HandlesTypes({Path.class})
public class MvcContainerInitializer extends KrazoContainerInitializer {
	@Override
	public void onStartup(Set<Class<?>> classes, ServletContext servletContext) {
		ApplicationEx app = ApplicationEx.getInstance();
		if(app != null) {
			if(LibraryUtil.usesLibrary(MvcLibrary.LIBRARY_ID, app)) {
				super.onStartup(classes, servletContext);
			}
		}
	}
}
