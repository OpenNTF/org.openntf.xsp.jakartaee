package org.openntf.xsp.cdi.inject;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

/**
 * Annotation that is paired with {@link NamedSession} to .
 * 
 * @since 2.11.0
 */
@Qualifier
@Documented
@Retention(RUNTIME)
public @interface SessionAs {
	/**
	 * The user name for the session, such as
	 * {@code "CN=Joe Schmoe/O=YourOrg"}.
	 * 
	 * <p>This value may also be an EL expression.</p>
	 * 
	 * @return the name of the user to create a session on behalf of
	 */
	String value();
	
	public static class Literal extends AnnotationLiteral<SessionAs> implements SessionAs {
	    private static final long serialVersionUID = 1L;

	    private final String value;

	    public static SessionAs of(String value) {
	        return new Literal(value);
	    }

	    public String value() {
	        return value;
	    }

	    private Literal(String value) {
	        this.value = value;
	    }

	}
}
