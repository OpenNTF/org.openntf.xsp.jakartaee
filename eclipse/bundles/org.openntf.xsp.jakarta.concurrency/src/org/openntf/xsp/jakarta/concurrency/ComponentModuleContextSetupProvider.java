package org.openntf.xsp.jakarta.concurrency;

import java.util.Map;

import org.glassfish.enterprise.concurrent.spi.ContextHandle;
import org.glassfish.enterprise.concurrent.spi.ContextSetupProvider;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;

import jakarta.enterprise.concurrent.ContextService;

public class ComponentModuleContextSetupProvider implements ContextSetupProvider {
	private static final long serialVersionUID = 1L;
	
	private final ComponentModule module;
	
	public ComponentModuleContextSetupProvider(ComponentModule module) {
		this.module = module;
	}

	@Override
	public ContextHandle saveContext(ContextService contextService) {
		
		return null;
	}

	@Override
	public ContextHandle saveContext(ContextService contextService, Map<String, String> contextObjectProperties) {
		
		return null;
	}

	@Override
	public ContextHandle setup(ContextHandle contextHandle) throws IllegalStateException {
		
		return null;
	}

	@Override
	public void reset(ContextHandle contextHandle) {
		
	}

}
