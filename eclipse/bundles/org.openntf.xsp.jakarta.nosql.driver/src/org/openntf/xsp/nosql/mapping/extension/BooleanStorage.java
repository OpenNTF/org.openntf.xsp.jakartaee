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

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.nosql.Column;

/**
 * This can be placed on a field annotated with {@link Column @Column} to
 * provide hints to the driver about how to store boolean values inside
 * the backing document.
 * 
 * <p>The type of entity field must be {@code boolean} or {@code Boolean}.</p>
 * 
 * @author Jesse Gallagher
 * @since 2.15.0
 */
@Documented
@Retention(RUNTIME)
@Target(FIELD)
public @interface BooleanStorage {
	enum Type {
		STRING, DOUBLE
	}
	
	/**
	 * @return the type of values to store
	 */
	Type type();
	
	/**
	 * @return the value to store for {@code true} when the
	 *         type is {@link Type#STRING}
	 */
	String stringTrue() default "Y";
	
	/**
	 * @return the value to store for {@code false} when the
	 *         type is {@link Type#STRING}
	 */
	String stringFalse() default "N";
	
	/**
	 * @return the value to store for {@code true} when the
	 *         type is {@link Type#DOUBLE}
	 */
	double doubleTrue() default 1;
	
	/**
	 * @return the value to store for {@code false} when the
	 *         type is {@link Type#DOUBLE}
	 */
	double doubleFalse() default 0;
}
