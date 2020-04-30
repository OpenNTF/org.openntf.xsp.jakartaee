package org.openntf.xsp.cdi.session;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.domino.osgi.core.context.ContextInfo;
import com.ibm.domino.xsp.adapter.osgi.NotesContext;
import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.application.DesignerApplicationEx;

import org.openntf.xsp.cdi.session.SessionScopeContextHolder.SessionScopeInstance;

/**
 * @since 2020-04
 *
 */
public class SessionScopeContext implements Context, Serializable {
	private static final long serialVersionUID = 1L;
	
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

	public static final String KEY_ID = SessionScopeContext.class.getName();
	public static final String KEY_BEANS = KEY_ID + "_beans"; //$NON-NLS-1$

	@Override
	public Class<? extends Annotation> getScope() {
		return SessionScoped.class;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(final Contextual<T> contextual, final CreationalContext<T> creationalContext) {
		Bean<T> bean = (Bean<T>) contextual;
		SessionScopeContextHolder beans = getHolder();
		if(beans.getBeans().containsKey(bean.getBeanClass().getName())) {
			return (T)beans.getBean(bean.getBeanClass().getName()).instance;
		} else {
			SessionScopeInstance<T> instance = new SessionScopeInstance<>();
			instance.beanClass = bean.getBeanClass().getName();
			instance.ctx = creationalContext;
			instance.instance = bean.create(creationalContext);
			beans.putBean(instance);
			return instance.instance;
		}
	}

	@SuppressWarnings({ "unchecked", "null" })
	@Override
	public <T> T get(final Contextual<T> contextual) {
		Bean<T> bean = (Bean<T>) contextual;
		SessionScopeContextHolder beans = getHolder();
		if(beans.getBeans().containsKey(bean.getBeanClass().getName())) {
			return (T)beans.getBean(bean.getBeanClass().getName()).instance;
		} else {
			return null;
		}
	}

	@Override
	public boolean isActive() {
		return true;
	}
	
	private SessionScopeContextHolder getHolder() {
		HttpSession session = getHttpSession();
		String key = null;
		
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if(facesContext != null) {
			ApplicationEx application = ApplicationEx.getInstance(facesContext);
			key = generateKey(((DesignerApplicationEx)application).getDesignerApplication().getAppName(), session);
		}
		
		// If we're not in a Faces context, check the OSGi servlet context
		NotesContext notesContext = NotesContext.getCurrentUnchecked();
		if(notesContext != null) {
			try {
				NotesDatabase database = ContextInfo.getServerDatabase();
				key = generateKey(database.getDatabasePath(), session);
			} catch (NotesAPIException e) {
				throw new RuntimeException(e);
			}
		}
		
		if(key == null) {
			throw new IllegalStateException("Unable to locate context database");
		}
		
		SessionScopeContextHolder holder = (SessionScopeContextHolder)session.getAttribute(key);
		if(holder == null) {
			holder = new SessionScopeContextHolder();
			session.setAttribute(key, holder);
		}
		return holder;
	}
	
	private static String generateKey(String dbPath, HttpSession session) {
		String normalizedPath = StringUtil.toString(dbPath)
			.toLowerCase()
			.replace('\\', '/');
		return SessionScopeContext.class.getName() + '-' + normalizedPath + '-' + session.getId();
	}

	private HttpSession getHttpSession() {
		// Check the active session
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if(facesContext != null) {
			HttpServletRequest req = (HttpServletRequest)facesContext.getExternalContext().getRequest();
			return req.getSession();
		}
		
		// If we're not in a Faces context, check the OSGi servlet context
		NotesContext notesContext = NotesContext.getCurrentUnchecked();
		if(notesContext != null) {
			HttpServletRequest req = getHttpServletRequest(notesContext);
			return req.getSession();
		}
		
		return null;
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
