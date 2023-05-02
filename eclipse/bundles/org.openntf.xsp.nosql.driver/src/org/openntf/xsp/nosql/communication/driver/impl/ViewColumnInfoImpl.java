package org.openntf.xsp.nosql.communication.driver.impl;

import java.util.Collection;
import java.util.EnumSet;

import org.openntf.xsp.nosql.communication.driver.ViewColumnInfo;

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

	public ViewColumnInfoImpl(String title, String programmaticName, SortOrder sortOrder,
			Collection<SortOrder> resortOrders, boolean categorized) {
		super();
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
