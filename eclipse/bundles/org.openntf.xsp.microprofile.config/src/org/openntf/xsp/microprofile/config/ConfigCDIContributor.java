package org.openntf.xsp.microprofile.config;

import java.util.Collection;
import java.util.Collections;

import org.openntf.xsp.cdi.discovery.WeldBeanClassContributor;
import org.openntf.xsp.jakartaee.LibraryUtil;

import com.ibm.xsp.application.ApplicationEx;
import io.smallrye.config.inject.ConfigExtension;
import jakarta.enterprise.inject.spi.Extension;

public class ConfigCDIContributor implements WeldBeanClassContributor {

	@Override
	public Collection<Class<?>> getBeanClasses() {
		return null;
	}

	@Override
	public Collection<Extension> getExtensions() {
		ApplicationEx application = ApplicationEx.getInstance();
		if(application != null) {
			if(LibraryUtil.usesLibrary(ConfigLibrary.LIBRARY_ID, application)) {
				return Collections.singleton(new ConfigExtension());	
			}
		}
		return null;
	}

}
