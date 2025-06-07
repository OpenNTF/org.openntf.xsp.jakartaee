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
package org.openntf.xsp.jakarta.nosql.communication.driver.lsxbe.util;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public abstract class AbstractCollectionIterator<E> implements Iterator<E> {
	protected final int size;
	protected int fetched = 0;

	public AbstractCollectionIterator(final int size) {
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
