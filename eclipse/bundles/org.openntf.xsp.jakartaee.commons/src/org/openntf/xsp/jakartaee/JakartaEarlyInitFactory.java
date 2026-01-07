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

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.text.MessageFormat;
import java.util.List;

import com.ibm.designer.runtime.domino.adapter.HttpService;
import com.ibm.designer.runtime.domino.adapter.IServiceFactory;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;

import org.openntf.xsp.jakartaee.events.JakartaHttpInitListener;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

/**
 * This stub {@link IServiceFactory} implementation exists to
 * ensure that this bundle is activated early in HTTP initialization
 * to run known Jakarta initializers and ensure that the weaving
 * hook is executed.
 *
 * @author Jesse Gallagher
 * @since 2.2.0
 */
public class JakartaEarlyInitFactory implements IServiceFactory {
	private static final Logger log = System.getLogger(JakartaEarlyInitFactory.class.getName());
	
	@Override
	public HttpService[] getServices(final LCDEnvironment env) {
		List<JakartaHttpInitListener> listeners = LibraryUtil.findExtensionsSorted(JakartaHttpInitListener.class, false);
		
		listeners.forEach(l -> {
			try {
				l.httpInit();
			} catch(Exception e) {
				log.log(Level.ERROR, () -> MessageFormat.format("Encountered exception running HTTP listener {0}", l), e);
			}
		});
		
		listeners.forEach(l -> {
			try {
				l.postInit();
			} catch(Exception e) {
				log.log(Level.ERROR, () -> MessageFormat.format("Encountered exception running HTTP listener post-init {0}", l), e);
			}
		});
		
		return new HttpService[0];
	}
}
