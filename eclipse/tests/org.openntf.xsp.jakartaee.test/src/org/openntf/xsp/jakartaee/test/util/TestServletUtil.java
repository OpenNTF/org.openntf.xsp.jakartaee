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
