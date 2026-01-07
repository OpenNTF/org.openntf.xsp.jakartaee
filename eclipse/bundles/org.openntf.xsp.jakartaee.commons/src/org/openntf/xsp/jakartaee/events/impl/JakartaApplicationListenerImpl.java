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
package org.openntf.xsp.jakartaee.events.impl;

import java.util.List;

import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.application.events.ApplicationListener2;

import org.openntf.xsp.jakartaee.events.JakartaApplicationListener;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

public class JakartaApplicationListenerImpl implements ApplicationListener2 {
	private List<JakartaApplicationListener> listeners;

	@Override
	public void applicationCreated(final ApplicationEx application) {
		listeners = LibraryUtil.findExtensionsSorted(JakartaApplicationListener.class, false);

		listeners.forEach(l -> l.applicationCreated(application));
	}

	@Override
	public void applicationDestroyed(final ApplicationEx application) {
		for(int i = listeners.size()-1; i >= 0; i--) {
			listeners.get(i).applicationDestroyed(application);
		}
	}

	@Override
	public void applicationRefreshed(final ApplicationEx application) {
		listeners.forEach(l -> l.applicationRefreshed(application));
	}

}
