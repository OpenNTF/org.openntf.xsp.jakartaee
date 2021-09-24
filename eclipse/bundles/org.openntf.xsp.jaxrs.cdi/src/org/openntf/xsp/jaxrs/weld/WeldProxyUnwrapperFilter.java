/**
 * Copyright © 2018-2021 Jesse Gallagher
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
package org.openntf.xsp.jaxrs.weld;

import java.io.IOException;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import org.jboss.weld.proxy.WeldClientProxy;
import org.jboss.weld.proxy.WeldClientProxy.Metadata;

/**
 * This {@link ContainerResponseFilter} checks for outgoing Weld bean proxies and unwraps them,
 * which avoids trouble with many {@link javax.ws.rs.ext.MessageBodyWriter MessageBodyWriter}
 * that rely on reflection and annotations.
 * 
 * @author Jesse Gallagher
 * @since 1.2.0
 */
@Provider
@Priority(Priorities.ENTITY_CODER)
public class WeldProxyUnwrapperFilter implements ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
			throws IOException {
		Object entity = responseContext.getEntity();
		if(entity instanceof WeldClientProxy) {
			Metadata metadata = ((WeldClientProxy)entity).getMetadata();
			responseContext.setEntity(metadata.getContextualInstance());
		}
	}
}
