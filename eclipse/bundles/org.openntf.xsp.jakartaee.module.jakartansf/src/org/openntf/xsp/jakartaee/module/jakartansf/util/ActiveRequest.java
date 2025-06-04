/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakartaee.module.jakartansf.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

import org.openntf.xsp.jakartaee.module.jakartansf.NSFJakartaModule;

import jakarta.servlet.http.HttpServletRequest;

public record ActiveRequest(NSFJakartaModule module, LSXBEHolder lsxbe, HttpServletRequest request) {

	private static ThreadLocal<Deque<ActiveRequest>> ACTIVE_REQUEST = ThreadLocal.withInitial(ArrayDeque::new);
	
	public static Optional<ActiveRequest> get() {
		return Optional.ofNullable(ACTIVE_REQUEST.get().peek());
	}
	public static void push(ActiveRequest request) {
		ACTIVE_REQUEST.get().push(request);
	}
	public static void pop() {
		ACTIVE_REQUEST.get().pop();
	}
	public static void pushRequest(HttpServletRequest request) {
		ActiveRequest active = ACTIVE_REQUEST.get().pop();
		if(active == null) {
			throw new IllegalStateException("Attempted to push a request on a non-active thread");
		}
		ACTIVE_REQUEST.get().push(new ActiveRequest(active.module, active.lsxbe, request));
		
	}
	
	public ActiveRequestCloner createCloner() {
		return new ActiveRequestCloner(module, request);
	}
}
