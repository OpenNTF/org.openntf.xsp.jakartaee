package org.openntf.xsp.jakartaee.xml;

import java.security.PrivilegedExceptionAction;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;

import java.security.AccessController;
import java.security.PrivilegedActionException;

/**
 * Utilities for working with XML Binding in a low-privilege context.
 * 
 * @since 2.0.0
 */
public enum JaxbUtil {
	;
	
	/**
	 * Creates a new {@link JAXBContext} instance for the provided classes.
	 * 
	 * <p>This method performs conversion in an {@link AccessController#doPrivileged} block
	 * to avoid permissions issues in an XPages application.</p>
	 * 
     * @param classesToBeBound
     *      list of java classes to be recognized by the new {@link JAXBContext}.
     *      Can be empty, in which case a {@link JAXBContext} that only knows about
     *      spec-defined classes will be returned.
     * @return
     *      A new instance of a <tt>JAXBContext</tt>. Always non-null valid object.
     * @throws JAXBException
     *      if an error was encountered while creating the
     *      <tt>JAXBContext</tt>, such as (but not limited to):
     * <ol>
     *  <li>No JAXB implementation was discovered
     *  <li>Classes use JAXB annotations incorrectly
     *  <li>Classes have colliding annotations (i.e., two classes with the same type name)
     *  <li>The JAXB implementation was unable to locate
     *      provider-specific out-of-band information (such as additional
     *      files generated at the development time.)
     * </ol>
     * @see JAXBContext#newInstance(Class...)
	 */
	public static JAXBContext newInstance(Class<?>... classes) throws JAXBException {
		try {
			return AccessController.doPrivileged((PrivilegedExceptionAction<JAXBContext>)() ->
				JAXBContext.newInstance(classes)
			);
		} catch (PrivilegedActionException e) {
			Throwable cause = e.getCause();
			if(cause instanceof JAXBException) {
				throw (JAXBException)cause;
			} else {
				throw new RuntimeException(e);
			}
		}
	}
}
