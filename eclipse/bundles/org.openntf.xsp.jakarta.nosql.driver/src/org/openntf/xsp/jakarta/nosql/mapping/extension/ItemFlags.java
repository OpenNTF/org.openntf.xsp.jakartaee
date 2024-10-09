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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.nosql.Column;

/**
 * This can be placed on a field annotated with {@link Column @Column} to
 * provide hints to the driver about the flags to apply to the item when
 * stored in a Domino document.
 *
 * @author Jesse Gallagher
 * @since 2.6.0
 */
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface ItemFlags {
	boolean authors() default false;
	boolean readers() default false;
	boolean names() default false;
	boolean encrypted() default false;
	boolean signed() default false;
	boolean protectedItem() default false;
	boolean summary() default true;
	/**
	 * Indicates that the item should not actually be written to storage
	 * when the backend document is saved.
	 * @since 2.7.0
	 */
	boolean saveToDisk() default true;
}
