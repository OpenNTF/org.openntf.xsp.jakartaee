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
package org.openntf.xsp.jakarta.el.impl;

import java.util.ResourceBundle;

import javax.faces.el.PropertyResolver;

import com.ibm.commons.util.io.json.JsonObject;
import com.ibm.jscript.types.FBSObject;
import com.ibm.xsp.el.PropertyResolverImpl;
import com.ibm.xsp.javascript.JSONPropertyResolver;
import com.ibm.xsp.javascript.JavaScriptPropertyResolver;
import com.ibm.xsp.model.DataObject;
import com.ibm.xsp.model.ViewRowData;
import com.ibm.xsp.model.domino.DominoDocumentPropertyResolver;

import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import lotus.domino.Document;

/**
 * An {@link ELResolver} instance that handles XPages-specific data types. Specifically:
 *
 * <ul>
 * 	<li>{@link ResourceBundle}<li>
 * 	<li>{@link ViewRowData}</li>
 *	<li>{@link DataObject}</li>
 *	<li>{@link JsonObject}</li>
 *	<li>{@link FBSObject}</li>
 *	<li>{@link Document}</li>
 * </ul>
 *
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class XSPELResolver extends ELResolver {

	@Override
	public Object getValue(final ELContext context, final Object base, final Object property) {
		PropertyResolver propertyResolver = getPropertyResolver(base);
		if(propertyResolver != null) {
			context.setPropertyResolved(true);
			return propertyResolver.getValue(base, property);
		}
		return null;
	}

	@Override
	public Class<?> getType(final ELContext context, final Object base, final Object property) {
		PropertyResolver propertyResolver = getPropertyResolver(base);
		if(propertyResolver != null) {
			context.setPropertyResolved(true);
			return propertyResolver.getType(base, property);
		}
		return null;
	}

	@Override
	public void setValue(final ELContext context, final Object base, final Object property, final Object value) {
		PropertyResolver propertyResolver = getPropertyResolver(base);
		if(propertyResolver != null) {
			context.setPropertyResolved(true);
			propertyResolver.setValue(base, property, value);
		}
	}

	@Override
	public boolean isReadOnly(final ELContext context, final Object base, final Object property) {
		PropertyResolver propertyResolver = getPropertyResolver(base);
		if(propertyResolver != null) {
			context.setPropertyResolved(true);
			return propertyResolver.isReadOnly(base, property);
		}
		return false;
	}

	@Override
	public Class<?> getCommonPropertyType(final ELContext context, final Object base) {
		if (base == null){
            return null;
        }

        return Object.class;
	}

	// *******************************************************************************
	// * Internal methods
	// *******************************************************************************

	private static PropertyResolverImpl ibmPropertyResolver = new PropertyResolverImpl();

	private PropertyResolver getPropertyResolver(final Object object) {
		if(object instanceof ResourceBundle) {
			return ibmPropertyResolver;
		} else if(object instanceof ViewRowData) {
			return ibmPropertyResolver;
		} else if(object instanceof DataObject) {
			return ibmPropertyResolver;
		} else if(object instanceof Document) {
			return DominoDocumentPropertyResolver.instance;
		} else if(object instanceof JsonObject) {
			return JSONPropertyResolver.instance;
		} else if(object instanceof FBSObject) {
			return JavaScriptPropertyResolver.instance;
		} else {
			return null;
		}
	}
}
