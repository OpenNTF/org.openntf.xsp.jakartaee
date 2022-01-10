package org.openntf.xsp.microprofile.fault.tolerance;

import java.util.Collection;
import java.util.Collections;

import org.openntf.xsp.cdi.discovery.WeldBeanClassContributor;
import org.openntf.xsp.jakartaee.LibraryUtil;

import com.ibm.xsp.application.ApplicationEx;

import io.smallrye.faulttolerance.FaultToleranceExtension;
import jakarta.enterprise.inject.spi.Extension;

public class FaultToleranceCDIContributor implements WeldBeanClassContributor {

	@Override
	public Collection<Class<?>> getBeanClasses() {
		return Collections.emptyList();
	}

	@Override
	public Collection<Extension> getExtensions() {
		ApplicationEx app = ApplicationEx.getInstance();
		if(app != null && LibraryUtil.usesLibrary(FaultToleranceLibrary.LIBRARY_ID, app)) {
			return Collections.singleton(new FaultToleranceExtension());
		} else {
			return Collections.emptyList();
		}
	}

}
