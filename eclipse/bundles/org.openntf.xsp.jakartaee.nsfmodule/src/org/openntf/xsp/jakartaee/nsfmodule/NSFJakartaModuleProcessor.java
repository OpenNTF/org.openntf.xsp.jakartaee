package org.openntf.xsp.jakartaee.nsfmodule;

import java.util.Map;
import java.util.stream.Stream;

import com.hcl.domino.module.nsf.RuntimeFileSystem.NSFFile;
import com.ibm.commons.util.StringUtil;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;

import org.openntf.xsp.jakartaee.module.ComponentModuleProcessor;
import org.openntf.xsp.jakartaee.util.ModuleUtil;

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
		return module.getRuntimeFileSystem().getAllResources().entrySet().stream()
			.map(Map.Entry::getKey)
			.filter(key -> key.startsWith(ModuleUtil.PREFIX_CLASSES) && key.endsWith(ModuleUtil.SUFFIX_CLASS))
			.map(key -> key.substring(ModuleUtil.PREFIX_CLASSES.length(), key.length()-ModuleUtil.SUFFIX_CLASS.length()))
			.map(key -> key.replace('/', '.'));
	}

	@Override
	public Stream<String> listFiles(NSFJakartaModule module, String basePath) {
		String path = basePath;
		boolean listAll = StringUtil.isEmpty(basePath);
		if(!listAll && !path.endsWith("/")) { //$NON-NLS-1$
			path += "/"; //$NON-NLS-1$
		}

		return module.getRuntimeFileSystem().getAllResources().entrySet().stream()
			.filter(entry -> entry.getValue() instanceof NSFFile)
			.map(Map.Entry::getKey)
			.filter(key -> listAll || key.startsWith(basePath));
	}
	
	@Override
	public String getModuleId(NSFJakartaModule module) {
		return module.getClass().getSimpleName() + "-" + module.getDelegate().getDatabasePath(); //$NON-NLS-1$
	}

}
