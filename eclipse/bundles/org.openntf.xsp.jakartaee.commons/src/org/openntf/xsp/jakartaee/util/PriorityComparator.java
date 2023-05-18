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
