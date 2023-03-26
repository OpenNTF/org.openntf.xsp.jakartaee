package org.openntf.xsp.cdi.bean;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.Set;

import org.openntf.xsp.cdi.inject.SessionAs;

import com.ibm.domino.napi.c.NotesUtil;
import com.ibm.domino.napi.c.Os;
import com.ibm.domino.napi.c.xsp.XSPNative;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import lotus.domino.Session;

/**
 * Represents a custom-named {@link Session} object inside a request.
 * 
 * @author Jesse Gallagher
 * @since 2.11.0
 */
public class SessionAsBean implements Bean<Session> {
	private static final Set<Type> TYPES = Collections.singleton(Session.class);
	
	private final SessionAs qualifier;
	private long hList;
	
	public SessionAsBean(SessionAs qualifier) {
		this.qualifier = qualifier;
	}

	@Override
	public Session create(CreationalContext<Session> creationalContext) {
		System.out.println("Asked to create in " + creationalContext);
		try {
			return AccessController.doPrivileged((PrivilegedExceptionAction<Session>) () -> {
				this.hList = NotesUtil.createUserNameList(qualifier.value());
				return XSPNative.createXPageSession(qualifier.value(), hList, true, false);
			});
		} catch (PrivilegedActionException e) {
			throw new RuntimeException("Encountered exception building session", e.getCause());
		}
	}

	@Override
	public void destroy(Session instance, CreationalContext<Session> creationalContext) {
		try {
			instance.recycle();
			Os.OSUnlock(hList);
			Os.OSMemFree(hList);
		} catch(Exception e) {
			// Nothing to be done
		}
	}

	@Override
	public Set<Type> getTypes() {
		return TYPES;
	}

	@Override
	public Set<Annotation> getQualifiers() {
		return Collections.singleton(this.qualifier);
	}

	@Override
	public Class<? extends Annotation> getScope() {
		return RequestScoped.class;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public Set<Class<? extends Annotation>> getStereotypes() {
		return Collections.emptySet();
	}

	@Override
	public boolean isAlternative() {
		return false;
	}

	@Override
	public Class<?> getBeanClass() {
		return Session.class;
	}

	@Override
	public Set<InjectionPoint> getInjectionPoints() {
		return Collections.emptySet();
	}

	@Override
	public boolean isNullable() {
		return false;
	}

}
