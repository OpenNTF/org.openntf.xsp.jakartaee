/*
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
package org.openntf.xsp.jakarta.nosql.mapping.extension;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * This annotation can be placed on a method in a {@link DominoRepository}
 * interface to indicate that the results should be pulled entirely
 * from the named view, without retrieving the back-end documents.
 *
 * @author Jesse Gallagher
 * @since 2.6.0
 *
 * @see {@link ViewCategory}
 */
@Documented
@Retention(RUNTIME)
@Target(METHOD)
public @interface ViewEntries {
	/**
	 * @return the name of the view to read
	 */
	String value();

	/**
	 * @return the maximum entry level to process
	 */
	int maxLevel() default -1;

	/**
	 * Sets whether view reading should only process document-type entries.
	 *
	 * @since 2.7.0
	 */
	boolean documentsOnly() default false;
	
	/**
	 * Sets whether to process special text in view column data.
	 * 
	 * <p>This processing adds overhead and so should only be enabled if needed.</p>
	 * 
	 * <p>Additionally, view entries will still only include data from columns that are
	 * included in the column values array. Columns that are considered static will not
	 * be included.</p> 
	 * 
	 * @since 3.8.0
	 */
	boolean processSpecialText() default false;
}
