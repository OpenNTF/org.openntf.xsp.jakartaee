/**
 * Copyright © 2018 Martin Pradny and Jesse Gallagher
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

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Path;
import javax.ws.rs.core.Application;

import org.openntf.xsp.jakartaee.ModuleUtil;

import com.ibm.domino.xsp.module.nsf.NSFComponentModule;
import com.ibm.domino.xsp.module.nsf.NotesContext;

/**
 * An {@link Application} subclass that searches the current module for resource classes.
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class NSFJAXRSApplication extends Application {

	public NSFJAXRSApplication() {
		
	}
	
	@Override
	public Set<Class<?>> getClasses() {
		NSFComponentModule module = NotesContext.getCurrent().getModule();
		return ModuleUtil.getClassNames(module)
			.filter(className -> !ModuleUtil.GENERATED_CLASSNAMES.matcher(className).matches())
			.map(className -> loadClass(module, className))
			.filter(this::isJAXRSClass)
			.collect(Collectors.toSet());
	}
	
	private boolean isJAXRSClass(Class<?> clazz) {
		if(clazz.getAnnotation(Path.class) != null) {
			return true;
		}
		
		if(Stream.of(clazz.getMethods()).anyMatch(m -> m.getAnnotation(Path.class) != null)) {
			return true;
		}
		
		return false;
	}
	
	private static Class<?> loadClass(NSFComponentModule module, String className) {
		try {
			return module.getModuleClassLoader().loadClass(className);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

}
