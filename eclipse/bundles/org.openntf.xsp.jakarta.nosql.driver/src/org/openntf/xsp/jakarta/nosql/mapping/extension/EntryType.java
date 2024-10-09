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
package org.openntf.xsp.jakarta.nosql.mapping.extension;

import org.openntf.xsp.jakarta.nosql.communication.driver.DominoConstants;

/**
 * This enum can be used in combination with {@link DominoConstants#FIELD_ENTRY_TYPE}
 * to identify the type of view entry when using the {@link ViewEntries @ViewEntries}
 * annotation.
 *
 * @author Jesse Gallagher
 * @since 2.6.0
 */
public enum EntryType {
	DOCUMENT, CATEGORY, TOTAL
}
