/**
 * Copyright (c) 2018-2023 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.mvc.jaxrs;

import org.openntf.xsp.jaxrs.exceptions.handler.HtmlExceptionHandler;
import org.openntf.xsp.jaxrs.exceptions.handler.RestExceptionHandler;

import jakarta.annotation.Priority;
import jakarta.mvc.Controller;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.MediaType;

/**
 * This exception handler will check if the incoming request appears
 * to be an MVC request and will handle it as HTML regardless
 * of whether or not there is a declared return type.
 * 
 * @author Jesse Gallagher
 * @since 2.10.0
 */
@Priority(RestExceptionHandler.DEFAULT_PRIORITY+1)
public class MvcExceptionHandler extends HtmlExceptionHandler {

	@Override
	public boolean canHandle(ResourceInfo resourceInfo, MediaType mediaType) {
		return isMvcRequest(resourceInfo);
	}
	
	private boolean isMvcRequest(ResourceInfo resourceInfo) {
		if(resourceInfo == null) {
			return false;
		}
		if(resourceInfo.getResourceClass() != null && resourceInfo.getResourceClass().isAnnotationPresent(Controller.class)) {
			return true;
		}
		if(resourceInfo.getResourceMethod() != null && resourceInfo.getResourceMethod().isAnnotationPresent(Controller.class)) {
			return true;
		}
		return false;
	}

}
