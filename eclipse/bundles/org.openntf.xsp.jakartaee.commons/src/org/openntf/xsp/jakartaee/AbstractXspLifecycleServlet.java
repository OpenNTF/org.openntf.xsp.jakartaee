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
package org.openntf.xsp.jakartaee;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.FacesContext;
import javax.servlet.Servlet;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.util.XSPErrorPage;
import com.ibm.xsp.acl.NoAccessSignal;
import com.ibm.xsp.context.FacesContextEx;
import com.ibm.xsp.controller.FacesController;
import com.ibm.xsp.webapp.DesignerFacesServlet;
import com.ibm.xsp.webapp.FacesServlet;

import org.openntf.xsp.jakartaee.servlet.ServletUtil;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.jakartaee.util.ModuleUtil;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HandlesTypes;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * An {@link HttpServlet} subclass that provides a Faces context to a
 * servlet request.
 *
 * @author Martin Pradny
 * @author Jesse Gallagher
 * @since 2.3.0
 */
public abstract class AbstractXspLifecycleServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(AbstractXspLifecycleServlet.class.getName());

	private static Method getFacesContextMethod;
	private static Method getContextFacesControllerMethod;
	static {
		AccessController.doPrivileged((PrivilegedAction<Void>)() -> {
			try {
				getFacesContextMethod = FacesServlet.class.getDeclaredMethod("getFacesContext", javax.servlet.ServletRequest.class, javax.servlet.ServletResponse.class); //$NON-NLS-1$
				getFacesContextMethod.setAccessible(true);
				getContextFacesControllerMethod = DesignerFacesServlet.class.getDeclaredMethod("getContextFacesController"); //$NON-NLS-1$
				getContextFacesControllerMethod.setAccessible(true);

				return null;
			} catch (NoSuchMethodException | SecurityException e) {
				throw new RuntimeException(e);
			}
		});
	}

	private ServletConfig config;
	private boolean initialized = false;
	private final ComponentModule module;
	private javax.servlet.Servlet facesServlet;
	private boolean doFaces;
	private boolean doEvents;

	public AbstractXspLifecycleServlet(final ComponentModule module) {
		this.module = module;
		this.doFaces = ModuleUtil.hasXPages(module);
		this.doEvents = ModuleUtil.emulateServletEvents(module);
	}

	@Override
	public void init(final ServletConfig config) throws ServletException {
		super.init(config);
		this.config = config;

		if(this.doEvents) {
			// Look for a web.xml file and populate init params
			ServletUtil.populateWebXmlParams(module, config.getServletContext());
			
			// Look for registered ServletContainerInitializers and emulate the behavior
			List<ServletContainerInitializer> initializers = LibraryUtil.findExtensions(ServletContainerInitializer.class);
			for(ServletContainerInitializer initializer : initializers) {
				Set<Class<?>> classes = null;
				if(initializer.getClass().isAnnotationPresent(HandlesTypes.class)) {
					classes = ModuleUtil.buildMatchingClasses(initializer.getClass().getAnnotation(HandlesTypes.class), module);
				}
				initializer.onStartup(classes, config.getServletContext());
			}
		} else {
			this.doInit(config, null);
		}

		// Kick off init early if needed
		if(this.doFaces) {
			this.getFacesServlet(config);
		}
	}

	@Override
	protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		FacesContext facesContext = null;
		try {
			response.setBufferSize(0);

			ModuleUtil.initializeSessionAsSigner(module);
			
			if(this.doEvents) {
				if (!initialized) {
					// For legacy modules, initialization has do be done after NotesContext is
					//   initialized with session to support sessionAsSigner operations
					doInit(config, request);
	
					initialized = true;
				}
			}

			if(this.doFaces) {
				// Do this for the side effects
				facesContext = getFacesContext(request, response);
		    	FacesContextEx exc = (FacesContextEx)facesContext;
		    	exc.getApplicationEx();
			}

	    	this.doService(request, response);
		} catch(NoAccessSignal t) {
			throw t;
		} catch(Throwable t) {
			if(log.isLoggable(Level.SEVERE)) {
				log.log(Level.SEVERE, "Encountered unhandled exception in Servlet", t);
			}

			try(PrintWriter w = response.getWriter()) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				XSPErrorPage.handleException(w, t, null, false);
			} catch (javax.servlet.ServletException e) {
				throw new IOException(e);
			} catch(IllegalStateException e) {
				// Happens when the writer or output has already been opened
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		} finally {
			if (facesContext != null) {
				releaseContext(facesContext);
			}
		}
	}

	@Override
    public ServletConfig getServletConfig() {
    	return config;
    }

	/**
	 * This method is called during the first request into the Servlet, to allow subclasses to
	 * perform delegate initialization or other tasks.
	 *
	 * <p>This delayed initialization allows delegate init to run at a point when the
	 * {@code NotesContext} is set up, which is not the case when calling
	 * {@link #init(ServletConfig}}.</p>
	 *
	 * @param config the active {@link ServletConfig}
	 * @param request the active {@link HttpServletRequest}
	 * @throws ServletException if initialization encounters a problem
	 */
	protected abstract void doInit(ServletConfig config, HttpServletRequest request) throws ServletException;

	protected abstract void doService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

	public ComponentModule getModule() {
		return module;
	}

	// *******************************************************************************
	// * Internal implementation methods
	// *******************************************************************************

	private synchronized Servlet getFacesServlet(final ServletConfig config) {
		if(this.facesServlet == null) {
			this.facesServlet = ModuleUtil.initXPagesServlet(this.module, getServletConfig())
				.orElseThrow(() -> new IllegalStateException(MessageFormat.format("Unable to initialize XPages FacesServlet for {0}", this.module)));
		}
		return this.facesServlet;
	}

	private FacesContext getFacesContext(final HttpServletRequest request, final HttpServletResponse response) {
		try {
			return (FacesContext)getFacesContextMethod.invoke(getFacesServlet(getServletConfig()), ServletUtil.newToOld(request, true), ServletUtil.newToOld(response));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	private void releaseContext(final FacesContext context) throws ServletException, IOException {
		context.responseComplete();
		try {
			FacesController controller = (FacesController)getContextFacesControllerMethod.invoke(facesServlet);
			controller.release(context);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// Not important - ignore
		}
    }
}
