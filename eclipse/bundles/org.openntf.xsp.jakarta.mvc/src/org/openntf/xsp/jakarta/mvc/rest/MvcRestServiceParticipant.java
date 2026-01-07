/**
 * Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.xsp.jakarta.mvc.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.krazo.bootstrap.DefaultConfigProvider;
import org.openntf.xsp.jakarta.rest.ServiceParticipant;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.jakartaee.util.ShimmingClassLoader;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This {@link ServiceParticipant} object adjusts the request ClassLoader to
 * account for some resource-loading needs of Krazo.
 *
 * @author Jesse Gallagher
 * @since 2.1.0
 */
public class MvcRestServiceParticipant implements ServiceParticipant {
	private static final String PROP_CLASSLOADER = MvcRestServiceParticipant.class.getName() + "_classloader"; //$NON-NLS-1$

	@Override
	public void doBeforeService(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		if(LibraryUtil.isLibraryActive(LibraryUtil.LIBRARY_UI)) {
			// Set a ClassLoader so that Krazo's ServiceLoader use can find these services
			ClassLoader current = Thread.currentThread().getContextClassLoader();
			request.setAttribute(PROP_CLASSLOADER, current);
			Thread.currentThread().setContextClassLoader(new KrazoClassLoader(current));
		}
	}

	@Override
	public void doAfterService(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		if(LibraryUtil.isLibraryActive(LibraryUtil.LIBRARY_UI)) {
			ClassLoader cl = (ClassLoader)request.getAttribute(PROP_CLASSLOADER);
			Thread.currentThread().setContextClassLoader(cl);
		}
	}

	// *******************************************************************************
	// * Internal implementation utilities
	// *******************************************************************************

	private static class KrazoClassLoader extends ClassLoader implements ShimmingClassLoader {
		private static final Bundle krazo;

		static {
			krazo = FrameworkUtil.getBundle(DefaultConfigProvider.class);
		}

		public KrazoClassLoader(final ClassLoader delegate) {
			super(delegate);
		}

		@Override
		protected Class<?> findClass(final String name) throws ClassNotFoundException {
			try {
				return krazo.loadClass(name);
			} catch(ClassNotFoundException e) {
				// Fall through
			}
			try {
				return super.findClass(name);
			} catch(ClassNotFoundException e) {
				throw new ClassNotFoundException("Unable to locate class " + name, e);
			}
		}

		@Override
		public URL getResource(final String name) {
			URL res = krazo.getResource(name);
			if(res != null) {
				return res;
			}
			return super.getResource(name);
		}

		@Override
		public InputStream getResourceAsStream(final String name) {
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
		public Enumeration<URL> getResources(final String name) throws IOException {
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
	}
}
