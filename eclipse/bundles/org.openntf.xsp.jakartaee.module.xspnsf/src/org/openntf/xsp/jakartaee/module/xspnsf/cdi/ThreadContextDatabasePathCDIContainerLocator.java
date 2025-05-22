package org.openntf.xsp.jakartaee.module.xspnsf.cdi;

import java.util.Optional;

import javax.servlet.ServletException;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;
import com.ibm.domino.xsp.module.nsf.NSFService;

import org.openntf.xsp.jakarta.cdi.ext.CDIContainerLocator;
import org.openntf.xsp.jakarta.cdi.util.ContainerUtil;

import jakarta.annotation.Priority;

/**
 * This {@link CDIContainerLocator} looks for a thread-context database path,
 * which may be specified as an override by user applications.
 *
 * @author Jesse Gallagher
 * @since 2.8.0
 */
@Priority(100)
public class ThreadContextDatabasePathCDIContainerLocator implements CDIContainerLocator {
	@Override
	public Object getContainer() {
		String nsfPath = ContainerUtil.getThreadContextDatabasePath();
		if(StringUtil.isNotEmpty(nsfPath)) {
			LCDEnvironment lcd = LCDEnvironment.getInstance();
			NSFService nsfService = lcd.getServices().stream()
				.filter(NSFService.class::isInstance)
				.map(NSFService.class::cast)
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("Unable to locate active NSFService"));
			try {
				return Optional.ofNullable(nsfService.loadModule(nsfPath));
			} catch(ServletException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}
}
