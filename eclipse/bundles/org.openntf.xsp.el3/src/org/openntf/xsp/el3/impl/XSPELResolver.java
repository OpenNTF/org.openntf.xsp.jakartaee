package org.openntf.xsp.el3.impl;

import java.beans.FeatureDescriptor;
import java.util.Collections;
import java.util.Iterator;
import java.util.ResourceBundle;

import javax.el.ELContext;
import javax.el.ELResolver;
import javax.faces.el.PropertyResolver;
import com.ibm.commons.util.io.json.JsonObject;
import com.ibm.jscript.types.FBSObject;
import com.ibm.xsp.el.PropertyResolverImpl;
import com.ibm.xsp.javascript.JSONPropertyResolver;
import com.ibm.xsp.javascript.JavaScriptPropertyResolver;
import com.ibm.xsp.model.DataObject;
import com.ibm.xsp.model.ViewRowData;
import com.ibm.xsp.model.domino.DominoDocumentPropertyResolver;

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
	public Object getValue(ELContext context, Object base, Object property) {
		PropertyResolver propertyResolver = getPropertyResolver(base);
		if(propertyResolver != null) {
			context.setPropertyResolved(true);
			return propertyResolver.getValue(base, property);
		}
		return null;
	}

	@Override
	public Class<?> getType(ELContext context, Object base, Object property) {
		PropertyResolver propertyResolver = getPropertyResolver(base);
		if(propertyResolver != null) {
			context.setPropertyResolved(true);
			return propertyResolver.getType(base, property);
		}
		return null;
	}

	@Override
	public void setValue(ELContext context, Object base, Object property, Object value) {
		PropertyResolver propertyResolver = getPropertyResolver(base);
		if(propertyResolver != null) {
			context.setPropertyResolved(true);
			propertyResolver.setValue(base, property, value);
		}
	}

	@Override
	public boolean isReadOnly(ELContext context, Object base, Object property) {
		PropertyResolver propertyResolver = getPropertyResolver(base);
		if(propertyResolver != null) {
			context.setPropertyResolved(true);
			return propertyResolver.isReadOnly(base, property);
		}
		return false;
	}

	@Override
	public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
		return Collections.emptyIterator();
	}

	@Override
	public Class<?> getCommonPropertyType(ELContext context, Object base) {
		if (base == null){
            return null;
        }

        return Object.class;
	}

	// *******************************************************************************
	// * Internal methods
	// *******************************************************************************
	
	private static PropertyResolverImpl ibmPropertyResolver = new PropertyResolverImpl();
	
	private PropertyResolver getPropertyResolver(Object object) {
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
