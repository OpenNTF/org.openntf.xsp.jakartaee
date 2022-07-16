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
