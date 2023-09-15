package org.openntf.xsp.jaxrs.exceptions;

import jakarta.annotation.Priority;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.Priorities;

/**
 * @since 2.14.0
 */
@Priority(Priorities.ENTITY_CODER+2)
public class NotAuthorizedExceptionHandler extends AbstractForbiddenExceptionHandler<NotAuthorizedException> {
}
