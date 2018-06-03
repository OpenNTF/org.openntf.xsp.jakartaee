/**
 * Copyright © 2018 Jesse Gallagher
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
package org.openntf.xsp.cdi.provider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.CDIProvider;
import javax.faces.context.FacesContext;

import org.eclipse.core.runtime.Platform;
import org.jboss.weld.environment.se.WeldContainer;
import org.osgi.framework.Bundle;

import com.ibm.xsp.application.ApplicationEx;

/**
 * Provides access to the current application's Weld context.
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class NSFCDIProvider implements CDIProvider {

	@Override
	public CDI<Object> getCDI() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if(facesContext != null) {
			ApplicationEx application = ApplicationEx.getInstance(facesContext);
			if(application != null) {
				// Access via reflection to avoid classloader issues
				Bundle xspcdi = Platform.getBundle("org.openntf.xsp.cdi"); //$NON-NLS-1$
				try {
					Class<?> util = xspcdi.loadClass("org.openntf.xsp.cdi.util.ContainerUtil"); //$NON-NLS-1$
					Method getContainer = util.getMethod("getContainer", ApplicationEx.class); //$NON-NLS-1$
					return (WeldContainer)getContainer.invoke(null, application);
				} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		
		return null;
	}
	
	@Override
	public int getPriority() {
		return DEFAULT_CDI_PROVIDER_PRIORITY+1;
	}

}
