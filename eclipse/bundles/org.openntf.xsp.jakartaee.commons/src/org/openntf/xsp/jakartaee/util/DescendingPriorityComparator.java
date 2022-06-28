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
