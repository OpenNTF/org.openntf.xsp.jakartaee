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
package org.openntf.xsp.jakartaee.module.jakartansf.concurrency;

import java.util.Optional;
import java.util.Properties;

import org.openntf.xsp.jakarta.concurrency.AbstractServletConcurrencyContainer;
import org.openntf.xsp.jakartaee.module.jakartansf.NSFJakartaModule;
import org.openntf.xsp.jakartaee.module.jakartansf.util.ActiveRequest;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class NSFJakartaModuleConcurrencyListener extends AbstractServletConcurrencyContainer
		implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		ActiveRequest.get().ifPresent(req -> {
			Properties props = LibraryUtil.getXspProperties(req.module());
			this.initializeConcurrencyContainer(props::getProperty);
		});
	}
	
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		this.terminateConcurrencyContainer();
	}
	
	@Override
	protected Optional<ServletContext> getServletContext() {
		return ActiveRequest.get()
			.map(ActiveRequest::module)
			.map(NSFJakartaModule::getJakartaServletContext);
	}

}
