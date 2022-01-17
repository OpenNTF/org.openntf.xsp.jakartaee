package org.openntf.xsp.jaxrs.ext;

import java.io.OutputStream;

/**
 * Represents an extension interface that a JSON implementation can use
 * to register a JSON-emitting generic exception mapper.
 * 
 * @author Jesse Gallagher
 * @since 2.3.0
 */
public interface JsonExceptionMapper {
	void writeJsonException(OutputStream os, Throwable throwable, String message);
	
	void writeNotFoundException(OutputStream os, Throwable throwable);
}
