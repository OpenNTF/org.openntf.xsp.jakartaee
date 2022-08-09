/**
 * Copyright © 2018-2022 Contributors to the XPages Jakarta EE Support Project
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

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated parameter in a {@link DominoRepository}
 * method should be used as a key for finding one or more entries in
 * a view.
 * 
 * <p>This can be applied to one or more parameters. If applied to a single
 * scalar parameter (such as a String), the parameter will be the only key.
 * If applied to a {@link java.util.Collection Collection} parameter, then
 * the value will be used as a multi-column key. If applied to multiple scalar
 * parameters, they will be combined into a single multi-column key.</p>
 * 
 * @author Jesse Gallagher
 * @since 2.8.0
 */
@Documented
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface ViewKey {
	/**
	 * Indicates whether the key search should be an exact match.
	 * 
	 * <p>If this annotation is specified for multiple parameters, then only
	 * the first instance is queried for this setting.</p>
	 * 
	 * @return {@code true} (default) if the key lookup should be exact;
	 *         {@code false} to allow substring matches
	 */
	boolean exact() default true;
}
