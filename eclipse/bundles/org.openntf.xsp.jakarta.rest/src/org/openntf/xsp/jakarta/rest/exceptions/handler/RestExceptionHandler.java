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
package org.openntf.xsp.jakarta.rest.exceptions.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * This service interface defines handlers for REST exceptions based on the
 * determined type of the endpoint and request.
 *
 * <p>Handlers of this type can be prioritied with the
 * {@link jakarta.annotation.Priority Priority} annotation, with higher values
 * taking precedence over lower values.</p>
 *
 * @author Jesse Gallagher
 * @since 2.10.0
 */
public interface RestExceptionHandler {
	int DEFAULT_PRIORITY = 0;

	boolean canHandle(ResourceInfo resourceInfo, MediaType mediaType);

	Response handle(final Throwable throwable, final int status, ResourceInfo resourceInfo, HttpServletRequest req);
}
