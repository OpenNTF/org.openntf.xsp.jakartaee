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
package org.openntf.xsp.jakarta.nosql.communication.driver.impl;

import java.util.Collection;
import java.util.EnumSet;

import org.openntf.xsp.jakarta.nosql.communication.driver.ViewColumnInfo;

/**
 * Basic implementation of {@link ViewColumnInfo}.
 *
 * @since 2.12.0
 */
public class ViewColumnInfoImpl implements ViewColumnInfo {

	private final String title;
	private final String programmaticName;
	private final SortOrder sortOrder;
	private final Collection<SortOrder> resortOrders;
	private final boolean categorized;

	public ViewColumnInfoImpl(final String title, final String programmaticName, final SortOrder sortOrder,
			final Collection<SortOrder> resortOrders, final boolean categorized) {
		this.title = title;
		this.programmaticName = programmaticName;
		this.sortOrder = sortOrder == null ? SortOrder.NONE : sortOrder;
		this.resortOrders = resortOrders == null ? EnumSet.noneOf(SortOrder.class) : EnumSet.copyOf(resortOrders);
		this.categorized = categorized;
	}

	@Override
	public String getTitle() {
		return this.title;
	}

	@Override
	public String getProgrammaticName() {
		return this.programmaticName;
	}

	@Override
	public SortOrder getSortOrder() {
		return this.sortOrder;
	}

	@Override
	public Collection<SortOrder> getResortOrders() {
		return this.resortOrders;
	}

	@Override
	public boolean isCategorized() {
		return this.categorized;
	}

}
