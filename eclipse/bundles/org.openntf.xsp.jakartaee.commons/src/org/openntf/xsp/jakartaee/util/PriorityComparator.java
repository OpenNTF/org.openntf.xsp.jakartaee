/**
 * Copyright (c) 2018-2023 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakartaee.util;

import java.util.Comparator;
import java.util.Optional;

import jakarta.annotation.Priority;

public enum PriorityComparator implements Comparator<Object> {
	ASCENDING(true), DESCENDING(false);
	
	private final boolean ascending;
	
	private PriorityComparator(boolean ascending) {
		this.ascending = ascending;
	}

	@Override
	public int compare(Object a, Object b) {
		int priorityA = Optional.ofNullable(a.getClass().getAnnotation(Priority.class))
			.map(Priority::value)
			.orElse(ascending ? Integer.MAX_VALUE : 0);
		int priorityB = Optional.ofNullable(b.getClass().getAnnotation(Priority.class))
			.map(Priority::value)
			.orElse(ascending ? Integer.MAX_VALUE : 0);
		if(ascending) {
			return Integer.compare(priorityA, priorityB);
		} else {
			return Integer.compare(priorityB, priorityA);
		}
	}

}
