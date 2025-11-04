package org.openntf.xsp.jakarta.nosql.mapping.extension;

import java.util.Set;

/**
 * Contains access information for an underlying database.
 * 
 * @since 3.6.0
 */
public record AccessRights(
	String name,
	AccessLevel level,
	Set<AccessPrivilege> privileges,
	Set<String> roles
) { }
