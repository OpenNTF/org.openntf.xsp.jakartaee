/**
 * Copyright © 2018-2022 Jesse Gallagher
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
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.faces.context.FacesContext;

import org.openntf.xsp.jakartaee.servlet.ServletUtil;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.jakartaee.util.ModuleUtil;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.util.XSPErrorPage;
import com.ibm.domino.xsp.module.nsf.NSFComponentModule;
import com.ibm.domino.xsp.module.nsf.NotesContext;
import com.ibm.domino.xsp.module.nsf.RuntimeFileSystem;
import com.ibm.xsp.acl.NoAccessSignal;
import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.context.FacesContextEx;
import com.ibm.xsp.controller.FacesController;
import com.ibm.xsp.webapp.DesignerFacesServlet;
import com.ibm.xsp.webapp.FacesServlet;

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
	private DesignerFacesServlet facesServlet;

	public AbstractXspLifecycleServlet(ComponentModule module) {
		this.module = module;
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.config = config;
		
		// Look for registered ServletContainerInitializers and emulate the behavior
		List<ServletContainerInitializer> initializers = LibraryUtil.findExtensions(ServletContainerInitializer.class);
		for(ServletContainerInitializer initializer : initializers) {
			Set<Class<?>> classes = null;
			if(initializer.getClass().isAnnotationPresent(HandlesTypes.class)) {
				classes = buildMatchingClasses(initializer.getClass().getAnnotation(HandlesTypes.class));
			}
			initializer.onStartup(classes, config.getServletContext());
		}

		// Kick off init early if needed
		this.getFacesServlet(config);
	}
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setBufferSize(0);
		
		initializeSessionAsSigner();
		FacesContext facesContext = null;
		try {
			if (!initialized){ // initialization has do be done after NotesContext is initialized with session to support SessionAsSigner operations
				doInit(config);
				
				initialized = true;
			}
			
			facesContext = getFacesContext(request, response);
	    	FacesContextEx exc = (FacesContextEx)facesContext;
	    	ApplicationEx application = exc.getApplicationEx();
	    	
	    	this.doService(request, response, application);
		} catch(NoAccessSignal t) {
			throw t;
		} catch(Throwable t) {
			try(PrintWriter w = response.getWriter()) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				XSPErrorPage.handleException(w, t, request.getRequestURL().toString(), false);
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
	 * @throws ServletException if initialization encounters a problem
	 */
	protected abstract void doInit(ServletConfig config) throws ServletException;
	
	protected abstract void doService(HttpServletRequest request, HttpServletResponse response, ApplicationEx application) throws ServletException, IOException;

	// *******************************************************************************
	// * Internal implementation methods
	// *******************************************************************************
	
	private synchronized FacesServlet getFacesServlet(ServletConfig config) {
		if(this.facesServlet == null) {
			try {
				this.facesServlet = (DesignerFacesServlet)module.getServlet("/foo.xsp").getServlet(); //$NON-NLS-1$
				// This should be functionally a NOP when already initialized
				this.facesServlet.init(ServletUtil.newToOld(config));
			} catch (javax.servlet.ServletException e) {
				throw new RuntimeException(e);
			}
		}
		return this.facesServlet;
	}
	
	private FacesContext getFacesContext(HttpServletRequest request, HttpServletResponse response) {
		try {
			return (FacesContext)getFacesContextMethod.invoke(getFacesServlet(getServletConfig()), ServletUtil.newToOld(request), ServletUtil.newToOld(response));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void releaseContext(FacesContext context) throws ServletException, IOException {
		context.responseComplete();
		try {
			FacesController controller = (FacesController)getContextFacesControllerMethod.invoke(facesServlet);
			controller.release(context);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
    }
	
	private void initializeSessionAsSigner() {
		NotesContext nc = NotesContext.getCurrentUnchecked();
		
		// This originally worked as below, but is now done reflectively to avoid trouble seen on 12.0.1
    	//String javaClassValue = "plugin.Activator"; //$NON-NLS-1$
		//String str = "WEB-INF/classes/" + javaClassValue.replace('.', '/') + ".class"; //$NON-NLS-1$ //$NON-NLS-2$
		//nc.setSignerSessionRights(str);

		// Use xsp.properties because it should exist in DBs built with NSF ODP Tooling
		String str = "WEB-INF/xsp.properties"; //$NON-NLS-1$
		RuntimeFileSystem.NSFFile res = (RuntimeFileSystem.NSFFile)nc.getModule().getRuntimeFileSystem().getResource(str);
		String signer = res.getUpdatedBy();
		
		AccessController.doPrivileged((PrivilegedAction<Void>)() -> {
			try {
				Field checkedSignersField = NotesContext.class.getDeclaredField("checkedSigners"); //$NON-NLS-1$
				checkedSignersField.setAccessible(true);
				@SuppressWarnings("unchecked")
				Set<String> checkedSigners = (Set<String>)checkedSignersField.get(nc);
				checkedSigners.clear();
				checkedSigners.add(signer);
				
				Field topLevelSignerField = NotesContext.class.getDeclaredField("toplevelXPageSigner"); //$NON-NLS-1$
				topLevelSignerField.setAccessible(true);
				topLevelSignerField.set(nc, signer);
				
				return null;
			} catch(Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	private Set<Class<?>> buildMatchingClasses(HandlesTypes types) {
		if(module instanceof NSFComponentModule) {
			// TODO consider whether we can handle other ComponentModules, were someone to make one
			
			@SuppressWarnings("unchecked")
			Set<Class<?>> result = ModuleUtil.getClassNames((NSFComponentModule)module)
				.filter(className -> !ModuleUtil.GENERATED_CLASSNAMES.matcher(className).matches())
				.map(className -> {
					try {
						return module.getModuleClassLoader().loadClass(className);
					} catch (ClassNotFoundException e) {
						throw new RuntimeException(e);
					}
				})
				.filter(c -> {
					for(Class<?> type : types.value()) {
						if(type.isAnnotation()) {
							return c.isAnnotationPresent((Class<? extends Annotation>)type);
						} else {
							return type.isAssignableFrom(c);
						}
					}
					return true;
				})
				.collect(Collectors.toSet());
			
			if(!result.isEmpty()) {
				return result;
			} else {
				return null;
			}
		}
		return null;
	}
}
