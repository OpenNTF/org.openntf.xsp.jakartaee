package org.openntf.xsp.nosql.communication.driver.lsxbe.util;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class AbstractCollectionIterator<E> implements Iterator<E> {
	protected final int size;
	protected int fetched = 0;
	
	public AbstractCollectionIterator(int size) {
		this.size = size;
	}

	@Override
	public boolean hasNext() {
		return fetched < size;
	}
	
	public Stream<E> stream() {
		Spliterator<E> iter = Spliterators.spliterator(this, size, Spliterator.DISTINCT | Spliterator.ORDERED | Spliterator.SIZED);
		return StreamSupport.stream(iter, false);
	}
}
