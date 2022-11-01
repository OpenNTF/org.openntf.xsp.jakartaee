package org.openntf.xsp.cdi.discovery;

import java.util.Collection;
import java.util.stream.Collectors;

import org.openntf.xsp.jakartaee.util.ModuleUtil;

import com.ibm.domino.xsp.module.nsf.NotesContext;

/**
 * This class is responsible for locating and loading bean classes from the
 * context NSF when active.
 * 
 * <p>Originally, this work was done by {@link NSFBeanArchiveHandler}, but
 * this mechanism avoids the trouble of handing off just string class names.</p>
 * 
 * @author Jesse Gallagher
 * @since 2.9.0
 */
public class NSFComponentModuleClassContributor implements WeldBeanClassContributor {

	@Override
	public Collection<Class<?>> getBeanClasses() {
		NotesContext notesContext = NotesContext.getCurrentUnchecked();
		if(notesContext != null) {
			return ModuleUtil.getClasses(notesContext.getModule())
				.collect(Collectors.toSet());
		}
		return null;
	}

}
