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
package org.openntf.xsp.nosql.mapping.extension;

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
}
