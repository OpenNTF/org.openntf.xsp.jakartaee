package org.openntf.xsp.microprofile.config;

import org.eclipse.microprofile.config.spi.ConfigProviderResolver;

import com.ibm.designer.runtime.domino.adapter.HttpService;
import com.ibm.designer.runtime.domino.adapter.IServiceFactory;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;

import io.smallrye.config.SmallRyeConfigBuilder;
import io.smallrye.config.SmallRyeConfigProviderResolver;
import io.smallrye.config.SysPropConfigSource;

public class ConfigInitFactory implements IServiceFactory {

	@Override
	public HttpService[] getServices(LCDEnvironment env) {
		SmallRyeConfigProviderResolver resolver = new SmallRyeConfigProviderResolver() {
			@Override
			public SmallRyeConfigBuilder getBuilder() {
				SmallRyeConfigBuilder builder = super.getBuilder();

				// Manualy add sources that would come from ServiceLoader
				builder = builder.withSources(new SysPropConfigSource());
				
				
				return builder;
			}
		};
		
		ConfigProviderResolver.setInstance(resolver);
		
		return null;
	}

}
