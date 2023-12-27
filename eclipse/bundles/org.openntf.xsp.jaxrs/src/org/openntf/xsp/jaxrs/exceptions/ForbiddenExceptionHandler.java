package org.openntf.xsp.jaxrs.exceptions;

import jakarta.annotation.Priority;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.Priorities;

/**
 * @since 2.14.0
 */
@Priority(Priorities.ENTITY_CODER+1)
public class ForbiddenExceptionHandler extends AbstractForbiddenExceptionHandler<ForbiddenException> {
}
