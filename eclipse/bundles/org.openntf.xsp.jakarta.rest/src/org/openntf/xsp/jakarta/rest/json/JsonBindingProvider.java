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
package org.openntf.xsp.jakarta.rest.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.weld.proxy.WeldClientProxy;
import org.jboss.weld.proxy.WeldClientProxy.Metadata;
import org.openntf.xsp.jakarta.json.JSONBindUtil;
import org.openntf.xsp.jakartaee.servlet.ServletUtil;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Providers;

@Produces({"application/json", "application/*+json", "text/json", "*/*"})
@Consumes({"application/json", "application/*+json", "text/json", "*/*"})
public class JsonBindingProvider implements MessageBodyWriter<Object>, MessageBodyReader<Object> {
	private static final Logger log = Logger.getLogger(JsonBindingProvider.class.getPackage().getName());

	public static final String PROP_STREAM = "rest.jsonb.stream"; //$NON-NLS-1$

	@Context
	private Providers providers;

	@Context
	private Application application;

	protected Jsonb getJsonb(final Class<?> type) {
		ContextResolver<Jsonb> resolver = providers.getContextResolver(Jsonb.class, MediaType.WILDCARD_TYPE);
		if(resolver != null) {
			return resolver.getContext(Jsonb.class);
		} else {
			return JsonbBuilder.create();
		}
	}

	// *******************************************************************************
	// * MessageBodyReader
	// *******************************************************************************

	@Override
	public boolean isReadable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
		return isSupportedMediaType(mediaType);
	}

	@Override
	public Object readFrom(final Class<Object> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType,
			final MultivaluedMap<String, String> httpHeaders, final InputStream entityStream)
			throws IOException, WebApplicationException {
		try {
			Jsonb jsonb = getJsonb(type);
			return JSONBindUtil.fromJson(entityStream, jsonb, genericType);
		} catch(Exception e) {
			if(log.isLoggable(Level.SEVERE)) {
				log.log(Level.SEVERE, "Encountered exception reading JSON input", e);
			}
			throw e;
		}
	}

	// *******************************************************************************
	// * MessageBodyWriter
	// *******************************************************************************

	@Override
	public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
		return isSupportedMediaType(mediaType);
	}

	@Override
	public long getSize(final Object t, final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
		return -1;
	}

	@Override
	public void writeTo(final Object t, final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType,
			final MultivaluedMap<String, Object> httpHeaders, final OutputStream entityStream)
			throws IOException, WebApplicationException {
		try {
			Object obj = t;
			// It may be a CDI proxy - try to unwrap it if so
			if(obj instanceof WeldClientProxy proxy) {
				Metadata meta = proxy.getMetadata();
				obj = meta.getContextualInstance();
			}

			Jsonb jsonb = getJsonb(type);

			boolean stream = false;
			Application app = this.application;
			if(app != null) {
				Object streamProp = app.getProperties().get(PROP_STREAM);
				stream = !"false".equals(streamProp); //$NON-NLS-1$
			}

			if(stream) {
				JSONBindUtil.toJson(obj, jsonb, entityStream);
			} else {
				String json = JSONBindUtil.toJson(obj, jsonb);
				entityStream.write(json.getBytes(StandardCharsets.UTF_8));
			}
			entityStream.flush();
		} catch(Exception e) {
			if(ServletUtil.isClosedConnection(e)) {
				// Ignore
				return;
			}

			if(log.isLoggable(Level.SEVERE)) {
				log.log(Level.SEVERE, "Encountered exception writing JSON output", e);
			}
			throw e;
		}
	}

	// *******************************************************************************
	// * Internal utility methods
	// *******************************************************************************

	private static final String JSON = "json"; //$NON-NLS-1$
	private static final String PLUS_JSON = "+json"; //$NON-NLS-1$

	public static boolean isSupportedMediaType(final MediaType mediaType) {
		return JSON.equals(mediaType.getSubtype()) || mediaType.getSubtype().endsWith(PLUS_JSON);
	}

}
