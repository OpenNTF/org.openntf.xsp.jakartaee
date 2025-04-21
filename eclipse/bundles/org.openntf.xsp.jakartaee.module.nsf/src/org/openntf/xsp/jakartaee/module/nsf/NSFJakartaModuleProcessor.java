package org.openntf.xsp.jakartaee.module.nsf;

import java.util.stream.Stream;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;

import org.openntf.xsp.jakartaee.module.ComponentModuleProcessor;

/**
 * @since 3.4.0
 */
public class NSFJakartaModuleProcessor implements ComponentModuleProcessor<NSFJakartaModule> {

	@Override
	public boolean canProcess(ComponentModule module) {
		return module instanceof NSFJakartaModule;
	}

	@Override
	public Stream<String> getClassNames(NSFJakartaModule module) {
		return module.getModuleClassLoader().getClassNames().stream();
	}

	@Override
	public Stream<String> listFiles(NSFJakartaModule module, String basePath) {
		return module.listFiles(basePath);
	}
	
	@Override
	public String getModuleId(NSFJakartaModule module) {
		return module.getClass().getSimpleName() + '-' + module.getMapping().nsfPath();
	}
	
	@Override
	public boolean emulateServletEvents(NSFJakartaModule module) {
		return false;
	}

}
