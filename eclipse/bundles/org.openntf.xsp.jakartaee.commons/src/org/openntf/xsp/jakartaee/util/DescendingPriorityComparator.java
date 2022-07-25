/**
 * Copyright © 2018-2022 Contributors to the XPages Jakarta EE Support Project
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

import jakarta.annotation.Priority;

/**
 * This {@link Comparator} will compare two arbitrary objects based
 * on their {@link Priority} annotation value, descending.
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public enum DescendingPriorityComparator implements Comparator<Object> {
	INSTANCE;
	
	@Override
	public int compare(Object o1, Object o2) {
		return Integer.compare(getPriority(o2), getPriority(o2));
	}
	
	private int getPriority(Object o) {
		return o == null ? -1
			: !o.getClass().isAnnotationPresent(Priority.class) ? 0
			: o.getClass().getAnnotation(Priority.class).value();
	}

}
