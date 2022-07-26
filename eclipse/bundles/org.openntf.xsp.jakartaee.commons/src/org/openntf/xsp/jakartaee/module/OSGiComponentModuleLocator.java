package org.openntf.xsp.jakartaee.module;

import jakarta.annotation.Priority;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Optional;

import org.openntf.xsp.jakartaee.servlet.ServletUtil;

import com.ibm.domino.xsp.adapter.osgi.AbstractOSGIModule;
import com.ibm.domino.xsp.adapter.osgi.NotesContext;
import com.ibm.domino.xsp.module.nsf.NSFComponentModule;

/**
 * Locates an active {@link NSFComponentModule} when the current request
 * is in an OSGi Servlet or WebContainer context.
 * 
 * @author Jesse Gallagher
 * @since 2.8.0
 */
@Priority(2)
public class OSGiComponentModuleLocator implements ComponentModuleLocator {
	private static final Field osgiNotesContextRequestField;
	private static final Field osgiNotesContextModuleField;
	static {
		Field[] request = new Field[1];
		Field[] module = new Field[1];
		AccessController.doPrivileged((PrivilegedAction<Void>)() -> {
			Class<?> osgiContextClass = null;
			try {
				osgiContextClass = Class.forName("com.ibm.domino.xsp.adapter.osgi.NotesContext"); //$NON-NLS-1$
			} catch (ClassNotFoundException e1) {
				// In Notes or other non-full environment
				return null;
			}
			try {
				Field field = osgiContextClass.getDeclaredField("request"); //$NON-NLS-1$
				field.setAccessible(true);
				request[0] = field;
				
				field = osgiContextClass.getDeclaredField("module"); //$NON-NLS-1$
				field.setAccessible(true);
				module[0] = field;
			} catch (NoSuchFieldException | SecurityException e) {
				throw new RuntimeException(e);
			}
			
			return null;
		});
		
		osgiNotesContextRequestField = request[0];
		osgiNotesContextModuleField = module[0];
	}
	
	private boolean isAvailable() {
		return osgiNotesContextRequestField != null;
	}
	
	@Override
	public boolean isActive() {
		return isAvailable() && NotesContext.getCurrentUnchecked() != null;
	}

	@Override
	public AbstractOSGIModule getActiveModule() {
		if(!isAvailable()) {
			return null;
		}
		NotesContext osgiContext = NotesContext.getCurrentUnchecked();
		if(osgiContext != null) {
			try {
				return (AbstractOSGIModule)osgiNotesContextModuleField.get(osgiContext);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}

	@Override
	public Optional<ServletContext> getServletContext() {
		if(!isActive()) {
			return Optional.empty();
		}
		// TODO determine if this is useful when working in a WebContainer app
		AbstractOSGIModule module = getActiveModule();
		javax.servlet.ServletContext ctx = module.getServletContext();
		String contextPath = getServletRequest().get().getContextPath();
		return Optional.ofNullable(ServletUtil.oldToNew(contextPath, ctx));
	}
	
	@Override
	public Optional<HttpServletRequest> getServletRequest() {
		if(!isActive()) {
			return Optional.empty();
		}
		NotesContext osgiContext = NotesContext.getCurrentUnchecked();
		if(osgiContext != null) {
			try {
				javax.servlet.http.HttpServletRequest request = (javax.servlet.http.HttpServletRequest)osgiNotesContextRequestField.get(osgiContext);
				javax.servlet.ServletContext servletContext = getActiveModule().getServletContext();
				return Optional.of(ServletUtil.oldToNew(servletContext, request));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		return Optional.empty();
	}

	

}