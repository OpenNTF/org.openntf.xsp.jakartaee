package org.openntf.xsp.cdi.context;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.spi.CDI;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.weld.manager.BeanManagerImpl;
import org.openntf.xsp.cdi.util.ContainerUtil;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.domino.osgi.core.context.ContextInfo;
import com.ibm.domino.xsp.adapter.osgi.NotesContext;
import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.application.DesignerApplicationEx;

/**
 * @author Jesse Gallagher
 * @since 1.2.0
 */
public class SessionScopeContext extends AbstractIdentifiedContext {
	public static final Map<String, SessionScopeContext> contexts = new ConcurrentHashMap<>();
	
	private static final Field notesContextRequestField;
	static {
		notesContextRequestField = AccessController.doPrivileged((PrivilegedAction<Field>)() -> {
			try {
				Field field = NotesContext.class.getDeclaredField("request"); //$NON-NLS-1$
				field.setAccessible(true);
				return field;
			} catch (NoSuchFieldException | SecurityException e) {
				throw new RuntimeException(e);
			}
		});
	}
	
	public SessionScopeContext(String contextId, String key) {
		super(contextId, key, SessionScoped.class);
	}
	
	@Override
	public boolean isActive() {
		if(!super.isActive()) {
			return false;
		}
		
		// Check the active session
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if(facesContext != null) {
			ApplicationEx application = ApplicationEx.getInstance(facesContext);
			HttpServletRequest req = (HttpServletRequest)facesContext.getExternalContext().getRequest();
			HttpSession session = req.getSession();
			String key = generateKey(((DesignerApplicationEx)application).getDesignerApplication().getAppName(), session);
			return getId().equals(key);
		}
		
		// If we're not in a Faces context, check the OSGi servlet context
		NotesContext notesContext = NotesContext.getCurrentUnchecked();
		if(notesContext != null) {
			try {
				NotesDatabase database = ContextInfo.getServerDatabase();
				HttpServletRequest req = getHttpServletRequest(notesContext);
				HttpSession session = req.getSession();
				String key = generateKey(database.getDatabasePath(), session);
				return getId().equals(key);
			} catch (NotesAPIException e) {
				throw new RuntimeException(e);
			}
		}
		
		return false;
	}
	
	public static void inject(ApplicationEx application, HttpSession session) {
		String key = generateKey(((DesignerApplicationEx)application).getDesignerApplication().getAppName(), session);
		contexts.computeIfAbsent(key, id -> {
			BeanManagerImpl beanManager = ContainerUtil.getBeanManager(application);
			
			SessionScopeContext context = new SessionScopeContext(beanManager.getContextId(), id);
			beanManager.addContext(context);
			return context;
		});
	}
	
	public static void inject(NotesDatabase database, HttpSession session) {
		String key = generateKey(database.getDatabasePath(), session);
		contexts.computeIfAbsent(key, id -> {
			try {
				CDI<Object> container = ContainerUtil.getContainer(database);
				if(container == null) {
					// Try to find one from the main provider, which can use extensions
					container = CDI.current();
				}
				BeanManagerImpl beanManager = ContainerUtil.getBeanManager(container);
				
				SessionScopeContext context = new SessionScopeContext(beanManager.getContextId(), id);
				beanManager.addContext(context);
				return context;
			} catch (NotesAPIException | IOException e) {
				throw new RuntimeException(e);
			}
		});
	}
	
	public static void eject(ApplicationEx application, HttpSession session) {
		String key = generateKey(((DesignerApplicationEx)application).getDesignerApplication().getAppName(), session);
		SessionScopeContext context = contexts.remove(key);
		if(context != null) {
			context.invalidate();
		}
	}
	
	public static void eject(NotesDatabase database, HttpSession session) {
		String key = generateKey(database.getDatabasePath(), session);
		SessionScopeContext context = contexts.remove(key);
		if(context != null) {
			context.invalidate();
		}
	}
	
	private static String generateKey(String dbPath, HttpSession session) {
		String normalizedPath = StringUtil.toString(dbPath)
			.toLowerCase()
			.replace('\\', '/');
		return normalizedPath + '-' + session.getId();
	}
	
	private static HttpServletRequest getHttpServletRequest(NotesContext context) {
		return AccessController.doPrivileged((PrivilegedAction<HttpServletRequest>)() -> {
			try {
				return (HttpServletRequest)notesContextRequestField.get(context);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		});
	}
}