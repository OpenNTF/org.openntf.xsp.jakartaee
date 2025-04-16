package org.openntf.xsp.jakartaee.module;

import java.util.Map;
import java.util.stream.Stream;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.domino.xsp.module.nsf.NSFComponentModule;
import com.ibm.domino.xsp.module.nsf.RuntimeFileSystem.NSFFile;

import org.openntf.xsp.jakartaee.util.ModuleUtil;

public class NSFComponentModuleProcessor implements ComponentModuleProcessor<NSFComponentModule> {

	@Override
	public boolean canProcess(final ComponentModule module) {
		return module instanceof NSFComponentModule;
	}

	@Override
	public Stream<String> getClassNames(final NSFComponentModule module) {
		return module.getRuntimeFileSystem().getAllResources().entrySet().stream()
			.map(Map.Entry::getKey)
			.filter(key -> key.startsWith(ModuleUtil.PREFIX_CLASSES) && key.endsWith(ModuleUtil.SUFFIX_CLASS))
			.map(key -> key.substring(ModuleUtil.PREFIX_CLASSES.length(), key.length()-ModuleUtil.SUFFIX_CLASS.length()))
			.map(key -> key.replace('/', '.'));
	}

	@Override
	public Stream<String> listFiles(final NSFComponentModule module, final String basePath) {
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
	public String getModuleId(final NSFComponentModule module) {
		return module.getDatabasePath();
	}
	
	@Override
	public String getXspPrefix(final NSFComponentModule module) {
		return "/xsp"; //$NON-NLS-1$
	}

	@Override
	public boolean hasXPages(NSFComponentModule module) {
		return true;
	}
}
