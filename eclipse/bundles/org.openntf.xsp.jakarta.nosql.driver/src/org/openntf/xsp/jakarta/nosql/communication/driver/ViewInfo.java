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
package org.openntf.xsp.jakarta.nosql.communication.driver;

import java.util.List;

/**
 * Represents design information about a view or folder from a Domino data source.
 *
 * @since 2.12.0
 */
public interface ViewInfo {
	/**
	 * The type of a view in a data source.
	 */
	enum Type {
		VIEW, FOLDER
	}
	
	/**
	 * Retrieves the type of the view; namely, this determines whether
	 * the represented design element is a view or a folder.
	 * 
	 * @return a {@link Type} value for the design element, never
	 *         {@code null}
	 */
	Type getType();
	
	/**
	 * Retrieves the primary title of the view.
	 * 
	 * @return the title of the view, never {@code null}
	 */
	String getTitle();
	
	/**
	 * Retrieves the aliases of the view, if present.
	 * 
	 * @return a {@link List} of view aliases, never {@code null}
	 */
	List<String> getAliases();
	
	/**
	 * Retrieves the UNID of the view.
	 * 
	 * @return the UNID of the view, never {@code null}
	 */
	String getUnid();
	
	/**
	 * Retrieves the selection formula of the view.
	 *  
	 * @return the selection formula of the view, never {@code null}
	 */
	String getSelectionFormula();
	
	/**
	 * Retrieves information about the view's columns.
	 * 
	 * @return a {@link List} of {@link ViewColumnInfo} objects, never
	 *         {@code null}
	 */
	List<ViewColumnInfo> getColumnInfo();
}
