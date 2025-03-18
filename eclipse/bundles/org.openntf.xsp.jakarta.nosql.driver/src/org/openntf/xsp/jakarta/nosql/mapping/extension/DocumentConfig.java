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
package org.openntf.xsp.jakarta.nosql.mapping.extension;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation can be used on an entity class to indicate the
 * form configuration for stored documents.
 *
 * <p>This can be useful to distinguish the entity name from the form
 * name in the case that distinct entities from different databases have
 * the same form name.</p>
 *
 * @since 3.4.0
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface DocumentConfig {
	/**
	 * Allows specifying a distinct form name.
	 *
	 * <p>This can be useful to distinguish the entity name from the form
	 * name in the case that distinct entities from different databases have
	 * the same form name.</p>
	 *
	 * @return the value to use for the Form item in documents
	 */
	String formName();
}
