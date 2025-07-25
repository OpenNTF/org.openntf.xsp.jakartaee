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
package org.openntf.xsp.jakarta.cdi.ext;

/**
 * This IBM Commons extension interface represents an object capable of locating
 * a CDI container for a given class in Domino when normal mechanisms fail.
 *
 * @author Jesse Gallagher
 * @since 1.2.0
 */
public interface CDIContainerLocator {
	static final String EXTENSION_POINT = CDIContainerLocator.class.getName();

	/**
	 * Indicates that the CDI container should be loaded based on a given OSGi bundle.
	 *
	 * @return a symbolic name of a bundle, or {@code null} to skip providing this information
	 */
	default String getBundleId() {
		return null;
	}

	/**
	 * Directly provides a CDI container. It is assumed that the return is an instance of
	 * {@code jakarta.enterprise.inject.spi.CDI}.
	 *
	 * @return a {@code CDI} instance, or {@code null} to skip providing this information
	 */
	default Object getContainer() {
		return null;
	}
}
