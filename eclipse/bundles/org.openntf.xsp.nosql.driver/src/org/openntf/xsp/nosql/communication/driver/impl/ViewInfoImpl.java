/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.nosql.communication.driver.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openntf.xsp.nosql.communication.driver.ViewColumnInfo;
import org.openntf.xsp.nosql.communication.driver.ViewInfo;

/**
 * Basic implementation of {@link ViewInfo}.
 * 
 * @since 2.12.0
 */
public class ViewInfoImpl implements ViewInfo {
	private final Type type;
	private final String title;
	private final List<String> aliases;
	private final String unid;
	private final String selectionFormula;
	private final List<ViewColumnInfo> columnInfo;
	
	public ViewInfoImpl(Type type, String title, List<String> aliases, String unid, String selectionFormula, List<ViewColumnInfo> columnInfo) {
		this.type = type;
		this.title = title == null ? "" : title; //$NON-NLS-1$
		this.aliases = aliases == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(aliases));
		this.unid = unid == null ? "" : unid; //$NON-NLS-1$
		this.selectionFormula = selectionFormula == null ? "" : selectionFormula; //$NON-NLS-1$
		this.columnInfo = columnInfo == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(columnInfo));
	}

	@Override
	public Type getType() {
		return this.type;
	}

	@Override
	public String getTitle() {
		return this.title;
	}

	@Override
	public List<String> getAliases() {
		return this.aliases;
	}

	@Override
	public String getUnid() {
		return this.unid;
	}
	
	@Override
	public String getSelectionFormula() {
		return this.selectionFormula;
	}
	
	@Override
	public List<ViewColumnInfo> getColumnInfo() {
		return this.columnInfo;
	}

}
