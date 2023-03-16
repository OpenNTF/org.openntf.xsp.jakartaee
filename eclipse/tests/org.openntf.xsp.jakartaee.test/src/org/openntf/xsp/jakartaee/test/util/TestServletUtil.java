package org.openntf.xsp.jakartaee.test.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;

import com.ibm.domino.xsp.bridge.http.exception.XspCmdException;

/**
 * @author Jesse Gallagher
 * @since 2.11.0
 */
@SuppressWarnings("nls")
public class TestServletUtil {
	@Test
	public void testIgnorableExceptions() {
		{
			Exception e = new Exception("I should not match");
			assertFalse(ServletUtil.isClosedConnection(e));
		}
		{
			Exception e = new XspCmdException(2); // "Internal error"
			assertTrue(ServletUtil.isClosedConnection(e));
		}
		{
			Exception e = new XspCmdException(1); // "Error Reading Post Data"
			assertFalse(ServletUtil.isClosedConnection(e));
		}
		{
			Exception e = new XspCmdException(2, "    ");
			assertTrue(ServletUtil.isClosedConnection(e));
		}
		{
			Exception e = new XspCmdException(2);
			Exception e2 = new Exception("I am the top level", e);
			assertTrue(ServletUtil.isClosedConnection(e2));
		}
	}
}
