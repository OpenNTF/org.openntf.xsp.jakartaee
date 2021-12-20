/**
 * Copyright © 2018-2021 Martin Pradny and Jesse Gallagher
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
package org.openntf.xsp.jaxrs.impl;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.faces.FacesException;
import javax.faces.FactoryFinder;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.event.PhaseListener;
import javax.faces.lifecycle.Lifecycle;

import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.openntf.xsp.cdi.ext.CDIConstants;
import org.openntf.xsp.jakartaee.LibraryUtil;
import org.openntf.xsp.jakartaee.ModuleUtil;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;
import org.openntf.xsp.jaxrs.ServiceParticipant;

import com.ibm.commons.util.NotImplementedException;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.domino.xsp.module.nsf.NSFComponentModule;
import com.ibm.domino.xsp.module.nsf.NotesContext;
import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.context.FacesContextEx;
import com.ibm.xsp.controller.FacesController;
import com.ibm.xsp.controller.FacesControllerFactoryImpl;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HandlesTypes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * An {@link ServletContainer} subclass that provides a Faces context to the
 * servlet request.
 * 
 * @author Martin Pradny
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class FacesJAXRSServletContainer extends HttpServletDispatcher {
	private static final long serialVersionUID = 1L;
	
	private ServletConfig config;
	private FacesContextFactory contextFactory;
	private boolean initialized = false;
	private final ComponentModule module;

	public FacesJAXRSServletContainer(ComponentModule module) {
		this.module = module;
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		this.config = config;
		contextFactory = (FacesContextFactory) FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
		
		// Look for registered ServletContainerInitializers and emulate the behavior
		List<ServletContainerInitializer> initializers = LibraryUtil.findExtensions(ServletContainerInitializer.class);
		for(ServletContainerInitializer initializer : initializers) {
			Set<Class<?>> classes = null;
			if(initializer.getClass().isAnnotationPresent(HandlesTypes.class)) {
				classes = buildMatchingClasses(initializer.getClass().getAnnotation(HandlesTypes.class));
			}
			initializer.onStartup(classes, config.getServletContext());
		}
	}
	
	private javax.servlet.ServletContext getOldServletContext() {
		return ServletUtil.newToOld(getServletContext());
	}
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setAttribute(CDIConstants.CDI_JAXRS_REQUEST, "true"); //$NON-NLS-1$
		
		NotesContext nc = NotesContext.getCurrentUnchecked();
    	String javaClassValue = "plugin.Activator"; //$NON-NLS-1$
		String str = "WEB-INF/classes/" + javaClassValue.replace('.', '/') + ".class"; //$NON-NLS-1$ //$NON-NLS-2$
		nc.setSignerSessionRights(str);
		FacesContext fc=null;
		try {
			fc = initContext(request, response);
	    	FacesContextEx exc = (FacesContextEx)fc;
	    	ApplicationEx application = exc.getApplicationEx();
	    	if (application.getController() == null) {
	    		FacesController controller = new FacesControllerFactoryImpl().createFacesController(getOldServletContext());
	    		controller.init(null);
	    	}
	    	
	    	@SuppressWarnings("unchecked")
			List<ServiceParticipant> participants = (List<ServiceParticipant>)application.findServices(ServiceParticipant.EXTENSION_POINT);
	    	for(ServiceParticipant participant : participants) {
	    		participant.doBeforeService(request, response);
	    	}
			if (!initialized){ // initialization has do be done after NotesContext is initialized with session to support SessionAsSigner operations
				super.init();
				super.init(config);
				
				initialized = true;
			}
	    	
	    	try {
	    		super.service(request, response);
	    	} finally {
	    		for(ServiceParticipant participant : participants) {
		    		participant.doAfterService(request, response);
		    	}
	    	}
		} catch(Throwable t) {
			t.printStackTrace();
			response.sendError(500, "Application failed!"); //$NON-NLS-1$
		} finally {
			if (fc != null) {
				releaseContext(fc);
			}
			
		}
	}
	
	@Override
    public ServletConfig getServletConfig() {
    	return config;
    }
	
	// *******************************************************************************
	// * Internal implementation methods
	// *******************************************************************************
	
	private FacesContext initContext(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Create a temporary FacesContext and make it available
		javax.servlet.ServletContext context = ServletUtil.newToOld(getServletConfig().getServletContext());
		javax.servlet.http.HttpServletRequest req = ServletUtil.newToOld(request);
		javax.servlet.http.HttpServletResponse resp = ServletUtil.newToOld(response);
        return contextFactory.getFacesContext(context, req, resp, dummyLifeCycle);
    }
	
	private void releaseContext(FacesContext context) throws ServletException, IOException {
		context.release();
    }
	
	private static Lifecycle dummyLifeCycle = new Lifecycle() {
		@Override
		public void render(FacesContext context) throws FacesException {
			throw new NotImplementedException();
		}

		@Override
		public void removePhaseListener(PhaseListener listener) {
			throw new NotImplementedException();
		}

		@Override
		public PhaseListener[] getPhaseListeners() {
			throw new NotImplementedException();
		}

		@Override
		public void execute(FacesContext context) throws FacesException {
			throw new NotImplementedException();
		}

		@Override
		public void addPhaseListener(PhaseListener listener) {
			throw new NotImplementedException();
		}
	};

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
