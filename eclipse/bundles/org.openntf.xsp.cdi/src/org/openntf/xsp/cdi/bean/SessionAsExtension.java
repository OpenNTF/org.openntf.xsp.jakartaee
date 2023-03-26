package org.openntf.xsp.cdi.bean;

import java.util.HashSet;
import java.util.Set;

import org.openntf.xsp.cdi.inject.SessionAs;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessInjectionPoint;
import lotus.domino.Session;

/**
 * This CDI extension creates {@link SessionAsBean} objects for defined
 * {@link NamedSession @SessionAs} injection points.
 * 
 * @author Jesse Gallagher
 * @since 2.11.0
 */
public class SessionAsExtension implements Extension {
	private Set<String> names = new HashSet<>();
	
	<T> void observes(@Observes final ProcessInjectionPoint<T, Session> point) {
		point.getInjectionPoint().getQualifiers().stream()
			.filter(a -> SessionAs.class.equals(a.annotationType()))
			.map(SessionAs.class::cast)
			.map(SessionAs::value)
			.forEach(names::add);;
	}
	
	void onAfterBeanDiscovery(@Observes final AfterBeanDiscovery afterBeanDiscovery, final BeanManager beanManager) {
		names.forEach(name -> afterBeanDiscovery.addBean(new SessionAsBean(SessionAs.Literal.of(name))));
	}
}