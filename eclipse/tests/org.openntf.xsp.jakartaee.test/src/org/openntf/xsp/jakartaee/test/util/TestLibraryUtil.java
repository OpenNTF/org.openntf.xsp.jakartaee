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
package org.openntf.xsp.jakartaee.test.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

public class TestLibraryUtil {

	@Test
	public void testExtensions() {
		List<ExampleExtension> ext = LibraryUtil.findExtensions(ExampleExtension.class);
		assertNotNull(ext);
		assertEquals(3, ext.size());
		assertTrue(ext.stream().anyMatch(ExampleExtensionA.class::isInstance));
		assertTrue(ext.stream().anyMatch(ExampleExtensionB.class::isInstance));
		assertTrue(ext.stream().anyMatch(ExampleExtensionC.class::isInstance));
	}
	
	@Test
	public void testExtensionsAscending() {
		List<ExampleExtension> ext = LibraryUtil.findExtensionsSorted(ExampleExtension.class, true);
		assertNotNull(ext);
		assertEquals(3, ext.size());
		assertEquals(ExampleExtensionC.class, ext.get(0).getClass());
		assertEquals(ExampleExtensionB.class, ext.get(1).getClass());
		assertEquals(ExampleExtensionA.class, ext.get(2).getClass());
	}
	
	@Test
	public void testExtensionsDescending() {
		List<ExampleExtension> ext = LibraryUtil.findExtensionsSorted(ExampleExtension.class, false);
		assertNotNull(ext);
		assertEquals(3, ext.size());
		assertEquals(ExampleExtensionB.class, ext.get(0).getClass());
		assertEquals(ExampleExtensionC.class, ext.get(1).getClass());
		assertEquals(ExampleExtensionA.class, ext.get(2).getClass());
	}

}
