/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakarta.transaction.nsf;

import javax.servlet.ServletException;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.IServletFactory;
import com.ibm.designer.runtime.domino.adapter.ServletMatch;

import org.openntf.xsp.jakarta.transaction.servlet.TransactionRequestListener;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;

import jakarta.servlet.ServletContext;

/**
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public class TransactionConfiguratorServletFactory implements IServletFactory {
	@Override
	public void init(final ComponentModule module) {
		ServletContext context = ServletUtil.oldToNew(null, module.getServletContext());
		context.addListener(new TransactionRequestListener());
	}

	@Override
	public ServletMatch getServletMatch(final String contextPath, final String path) throws ServletException {
		// NOP
		return null;
	}

}
