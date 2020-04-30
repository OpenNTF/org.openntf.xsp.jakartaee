package org.openntf.xsp.cdi.context;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.openntf.xsp.cdi.context.BasicScopeContextHolder.BasicScopeInstance;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.domino.osgi.core.context.ContextInfo;
import com.ibm.domino.xsp.adapter.osgi.NotesContext;
import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.application.DesignerApplicationEx;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.2.0
 */
@SuppressWarnings("serial")
public abstract class AbstractProxyingContext implements Context, Serializable {
	
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

	protected abstract BasicScopeContextHolder getHolder();
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(final Contextual<T> contextual, final CreationalContext<T> creationalContext) {
		Bean<T> bean = (Bean<T>) contextual;
		BasicScopeContextHolder beans = getHolder();
		if(beans.getBeans().containsKey(bean.getBeanClass().getName())) {
			return (T)beans.getBean(bean.getBeanClass().getName()).instance;
		} else {
			BasicScopeInstance<T> instance = new BasicScopeInstance<>();
			instance.beanClass = bean.getBeanClass().getName();
			instance.ctx = creationalContext;
			instance.instance = bean.create(creationalContext);
			beans.putBean(instance);
			return instance.instance;
		}
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public <T> T get(final Contextual<T> contextual) {
		Bean<T> bean = (Bean<T>) contextual;
		BasicScopeContextHolder beans = getHolder();
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
	
	protected String generateKey() {
		String dbPath = null;
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if(facesContext != null) {
			ApplicationEx application = ApplicationEx.getInstance(facesContext);
			dbPath = ((DesignerApplicationEx)application).getDesignerApplication().getAppName();
		}
		
		// If we're not in a Faces context, check the OSGi servlet context
		NotesContext notesContext = NotesContext.getCurrentUnchecked();
		if(notesContext != null) {
			try {
				NotesDatabase database = ContextInfo.getServerDatabase();
				dbPath = database.getDatabasePath();
			} catch (NotesAPIException e) {
				throw new RuntimeException(e);
			}
		}
		
		if(StringUtil.isEmpty(dbPath)) {
			throw new IllegalStateException("Unable to locate context database");
		}
		
		String normalizedPath = StringUtil.toString(dbPath)
			.toLowerCase()
			.replace('\\', '/');
		return getClass().getName() + '-' + normalizedPath;
	}
	
	protected HttpServletRequest getHttpServletRequest() {
		// Check the active session
		FacesContext facesContext = FacesContext.getCurrentInstance();
		if(facesContext != null) {
			return (HttpServletRequest)facesContext.getExternalContext().getRequest();
		}
		
		// If we're not in a Faces context, check the OSGi servlet context
		NotesContext notesContext = NotesContext.getCurrentUnchecked();
		if(notesContext != null) {
			return getHttpServletRequest(notesContext);
		}
		
		return null;
	}
	
	protected HttpServletRequest getHttpServletRequest(NotesContext context) {
		return AccessController.doPrivileged((PrivilegedAction<HttpServletRequest>)() -> {
			try {
				return (HttpServletRequest)notesContextRequestField.get(context);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		});
	}
}
