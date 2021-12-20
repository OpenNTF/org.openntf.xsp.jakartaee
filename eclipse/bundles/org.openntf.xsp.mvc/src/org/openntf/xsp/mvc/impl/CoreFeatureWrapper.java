package org.openntf.xsp.mvc.impl;

import org.eclipse.krazo.bootstrap.CoreFeature;
import org.openntf.xsp.jakartaee.LibraryUtil;
import org.openntf.xsp.mvc.MvcLibrary;

import com.ibm.xsp.application.ApplicationEx;

import jakarta.ws.rs.ConstrainedTo;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.Provider;

@Provider
@ConstrainedTo(RuntimeType.SERVER)
public class CoreFeatureWrapper extends CoreFeature {

	@Override
	public boolean configure(FeatureContext context) {
		ApplicationEx app = ApplicationEx.getInstance();
		if(app != null) {
			if(LibraryUtil.usesLibrary(MvcLibrary.LIBRARY_ID, app)) {
				return super.configure(context);
			}
		}
		return false;
	}

}
