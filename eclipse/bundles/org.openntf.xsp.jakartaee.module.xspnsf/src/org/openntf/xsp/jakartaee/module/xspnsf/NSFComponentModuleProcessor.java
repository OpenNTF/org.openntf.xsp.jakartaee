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
package org.openntf.xsp.jakartaee.module.xspnsf;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.domino.xsp.module.nsf.NSFComponentModule;
import com.ibm.domino.xsp.module.nsf.NotesContext;
import com.ibm.domino.xsp.module.nsf.RuntimeFileSystem;
import com.ibm.domino.xsp.module.nsf.RuntimeFileSystem.NSFFile;

import org.openntf.xsp.jakartaee.module.ComponentModuleProcessor;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;
import org.openntf.xsp.jakartaee.util.ModuleUtil;

import jakarta.servlet.ServletConfig;

public class NSFComponentModuleProcessor implements ComponentModuleProcessor<NSFComponentModule> {

	@Override
	public boolean canProcess(final ComponentModule module) {
		return module instanceof NSFComponentModule;
	}

	@Override
	public Stream<String> getClassNames(final NSFComponentModule module) {
		return module.getRuntimeFileSystem().getAllResources().entrySet().stream()
			.map(Map.Entry::getKey)
			.filter(key -> key.startsWith(ModuleUtil.PREFIX_CLASSES) && key.endsWith(ModuleUtil.SUFFIX_CLASS))
			.map(key -> key.substring(ModuleUtil.PREFIX_CLASSES.length(), key.length()-ModuleUtil.SUFFIX_CLASS.length()))
			.map(key -> key.replace('/', '.'));
	}

	@Override
	public Stream<String> listFiles(final NSFComponentModule module, final String basePath) {
		String path = basePath;
		boolean listAll = StringUtil.isEmpty(basePath);
		if(!listAll && !path.endsWith("/")) { //$NON-NLS-1$
			path += "/"; //$NON-NLS-1$
		}

		return module.getRuntimeFileSystem().getAllResources().entrySet().stream()
			.filter(entry -> entry.getValue() instanceof NSFFile)
			.map(Map.Entry::getKey)
			.filter(key -> listAll || key.startsWith(basePath));
	}

	@Override
	public String getModuleId(final NSFComponentModule module) {
		return module.getDatabasePath();
	}
	
	@Override
	public String getXspPrefix(final NSFComponentModule module) {
		return "/xsp"; //$NON-NLS-1$
	}

	@Override
	public boolean hasXPages(NSFComponentModule module) {
		return true;
	}
	
	@Override
	public Optional<javax.servlet.Servlet> initXPagesServlet(NSFComponentModule module, ServletConfig servletConfig) {
		try {
			javax.servlet.Servlet facesServlet = module.getServlet("/foo.xsp").getServlet(); //$NON-NLS-1$
			// This should be functionally a NOP when already initialized
			facesServlet.init(ServletUtil.newToOld(servletConfig));
			return Optional.of(facesServlet);
		} catch (javax.servlet.ServletException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void initializeSessionAsSigner(NSFComponentModule module) {
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
}
