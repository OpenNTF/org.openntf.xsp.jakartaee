package org.openntf.xsp.jakartaee.module.osgi;

import java.util.stream.Stream;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.domino.xsp.adapter.osgi.AbstractOSGIModule;

import org.openntf.xsp.jakartaee.module.ComponentModuleProcessor;

/**
 * @since 3.4.0
 */
public class OSGiComponentModuleProcessor implements ComponentModuleProcessor<AbstractOSGIModule> {

	@Override
	public boolean canProcess(ComponentModule module) {
		return module instanceof AbstractOSGIModule;
	}

	@Override
	public Stream<String> getClassNames(AbstractOSGIModule module) {
		return Stream.empty();
	}

	@Override
	public Stream<String> listFiles(AbstractOSGIModule module, String basePath) {
		return Stream.empty();
	}
	
	@Override
	public boolean usesBundleClassLoader(AbstractOSGIModule module) {
		return true;
	}
	
	@Override
	public boolean hasImplicitCdi(AbstractOSGIModule module) {
		return true;
	}

}
