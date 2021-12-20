package org.openntf.xsp.mvc;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.krazo.bootstrap.DefaultConfigProvider;
import org.openntf.xsp.jakartaee.LibraryUtil;
import org.openntf.xsp.jaxrs.ServiceParticipant;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import com.ibm.xsp.application.ApplicationEx;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This {@link ServiceParticipant} object stashes the current Servlet request and
 * response objects during a JAX-RS request for use in Krazo beans.
 * 
 * @author Jesse Gallagher
 * @since 2.1.0
 */
public class MvcJaxrsServiceParticipant implements ServiceParticipant {
	public static final ThreadLocal<HttpServletRequest> CURRENT_REQUEST = new ThreadLocal<>();
	public static final ThreadLocal<HttpServletResponse> CURRENT_RESPONSE = new ThreadLocal<>();
	
	private static final ThreadLocal<ClassLoader> CLASSLOADERS = new ThreadLocal<>();

	@Override
	public void doBeforeService(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ApplicationEx app = ApplicationEx.getInstance();
		if(app != null) {
			if(LibraryUtil.usesLibrary(MvcLibrary.LIBRARY_ID, app)) {
				CURRENT_REQUEST.set(request);
				CURRENT_RESPONSE.set(response);
				
				ClassLoader current = Thread.currentThread().getContextClassLoader();
				CLASSLOADERS.set(current);
				
				// Provide a custom ClassLoader that knows about Krazo, since the current NSF
				//   one doesn't (oddly)
				Bundle krazo = FrameworkUtil.getBundle(DefaultConfigProvider.class);
				Thread.currentThread().setContextClassLoader(new ClassLoader(current) {
					@Override
					protected Class<?> findClass(String name) throws ClassNotFoundException {
						try {
							return krazo.loadClass(name);
						} catch(ClassNotFoundException e) {
							// Fall through
						}
						return super.findClass(name);
					}
					
					@Override
					public URL getResource(String name) {
						URL res = krazo.getResource(name);
						if(res != null) {
							return res;
						}
						return super.getResource(name);
					}
					
					@Override
					public InputStream getResourceAsStream(String name) {
						URL res = krazo.getResource(name);
						if(res != null) {
							try {
								return res.openStream();
							} catch (IOException e) {
								throw new UncheckedIOException(e);
							}
						}
						return super.getResourceAsStream(name);
					}
					
					@Override
					public Enumeration<URL> getResources(String name) throws IOException {
						List<URL> result = new ArrayList<>();
						
						Enumeration<URL> kres = krazo.getResources(name);
						if(kres != null) {
							result.addAll(Collections.list(kres));
						}
						Enumeration<URL> parent = super.getResources(name);
						if(parent != null) {
							result.addAll(Collections.list(parent));
						}
						return Collections.enumeration(result);
					}
				});
			}
		}
	}

	@Override
	public void doAfterService(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		ApplicationEx app = ApplicationEx.getInstance();
		if(app != null) {
			if(LibraryUtil.usesLibrary(MvcLibrary.LIBRARY_ID, app)) {
				CURRENT_REQUEST.set(null);
				CURRENT_RESPONSE.set(null);
				Thread.currentThread().setContextClassLoader(CLASSLOADERS.get());
			}
		}
	}

}
