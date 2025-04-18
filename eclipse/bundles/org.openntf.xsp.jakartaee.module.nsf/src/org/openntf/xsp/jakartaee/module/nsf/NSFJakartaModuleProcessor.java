package org.openntf.xsp.jakartaee.module.nsf;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesCollectionEntry;
import com.ibm.designer.domino.napi.NotesConstants;
import com.ibm.designer.domino.napi.util.NotesUtils;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;

import org.openntf.xsp.jakartaee.module.ComponentModuleProcessor;
import org.openntf.xsp.jakartaee.module.nsf.io.DesignCollectionIterator;

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
		String path = basePath;
		boolean listAll = StringUtil.isEmpty(basePath);
		if(!listAll && !path.endsWith("/")) { //$NON-NLS-1$
			path += "/"; //$NON-NLS-1$
		}
		
		List<String> result = new ArrayList<>();
		try (DesignCollectionIterator nav = new DesignCollectionIterator(module.getNotesDatabase())) {
			while (nav.hasNext()) {
				NotesCollectionEntry entry = nav.next();

				String flags = entry.getItemValueAsString(NotesConstants.DESIGN_FLAGS);
				if(NotesUtils.CmemflagTestMultiple(flags, NotesConstants.DFLAGPAT_FILE)) {
					// In practice, we don't care about $ClassIndexItem
					String name = entry.getItemValueAsString(NotesConstants.FIELD_TITLE);
					if(NotesUtils.CmemflagTestMultiple(flags, NotesConstants.DFLAGPAT_JAVAJAR)) {
						name = "WEB-INF/lib/" + name; //$NON-NLS-1$
					}
					result.add(name);
				}

				entry.recycle();
			}
		} catch (NotesAPIException e) {
			throw new RuntimeException(e);
		}

		return result.stream();
	}
	
	@Override
	public String getModuleId(NSFJakartaModule module) {
		return module.getClass().getSimpleName() + "-" + module.getDelegate().getDatabasePath(); //$NON-NLS-1$
	}
	
	@Override
	public boolean emulateServletEvents(NSFJakartaModule module) {
		return false;
	}

}
