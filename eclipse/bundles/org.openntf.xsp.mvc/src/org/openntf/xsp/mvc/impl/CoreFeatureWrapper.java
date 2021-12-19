package org.openntf.xsp.mvc.impl;

import org.eclipse.krazo.bootstrap.CoreFeature;

import jakarta.ws.rs.ConstrainedTo;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.Provider;

@Provider
@ConstrainedTo(RuntimeType.SERVER)
public class CoreFeatureWrapper extends CoreFeature {

	@Override
	public boolean configure(FeatureContext context) {
		// TODO Limit to only when the NSF opts in
		
		return super.configure(context);
	}

}
