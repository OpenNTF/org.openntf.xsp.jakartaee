/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jaxrs.exceptions.handler;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.FactoryFinder;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.servlet.ServletException;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.runtime.domino.adapter.util.XSPErrorPage;
import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.renderkit.ReadOnlyRenderKitFactory;

import jakarta.annotation.Priority;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

/**
 * This handle will render exceptions using the server-default XPages
 * exception page when the media type is {@code text/html}.
 * 
 * @author Jesse Gallagher
 * @since 2.10.0
 */
@Priority(RestExceptionHandler.DEFAULT_PRIORITY)
public class HtmlExceptionHandler implements RestExceptionHandler {
	private static final Logger log = Logger.getLogger(HtmlExceptionHandler.class.getName());

	@Override
	public boolean canHandle(ResourceInfo resourceInfo, MediaType mediaType) {
		return MediaType.TEXT_HTML_TYPE.isCompatible(mediaType);
	}

	@Override
	public Response handle(Throwable throwable, int status, ResourceInfo resourceInfo, HttpServletRequest req) {
		return Response.status(status)
			.type(MediaType.TEXT_HTML_TYPE)
			.entity((StreamingOutput)out -> {
				try {
					// Check for an error page in the DB
					FacesContext facesContext = FacesContext.getCurrentInstance();
					if(facesContext != null) {
						ApplicationEx app = (ApplicationEx)facesContext.getApplication();
						String defaultPage = app.getProperty("xsp.error.page.default", null); //$NON-NLS-1$
						if(!"true".equals(defaultPage)) { //$NON-NLS-1$
							// Then check for a customized page
							String errorPage = app.getProperty("xsp.error.page", null); //$NON-NLS-1$
							if(StringUtil.isNotEmpty(errorPage)) {
								// Then push the error to the request and render the XPage
								req.setAttribute("error", throwable); //$NON-NLS-1$
								ViewHandler viewHandler = app.getViewHandler();
								UIViewRoot viewRoot = viewHandler.createView(facesContext, errorPage);
								facesContext.setViewRoot(viewRoot);
								
								// The renderkit factory will be null outside a true XPage request
								if(FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY) == null) {
									FactoryFinder.setFactory(FactoryFinder.RENDER_KIT_FACTORY, ReadOnlyRenderKitFactory.class.getName());
								}
								
								app.getController().render(facesContext, viewRoot);
								
								return;
							}
						}
					}
					
					try(PrintWriter w = new PrintWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
						XSPErrorPage.handleException(w, throwable, null, false);
					} catch (ServletException e) {
						throw new IOException(e);
					}
				} catch(Throwable t) {
					if(log.isLoggable(Level.SEVERE)) {
						log.log(Level.SEVERE, "Encountered exception writing HTML exception output", t);
						log.log(Level.SEVERE, "Original exception", throwable);
					}
					throw t;
				}
			})
			.build();
	}

}
