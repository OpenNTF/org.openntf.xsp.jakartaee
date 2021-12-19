package org.openntf.xsp.jsp.el;

import jakarta.el.BeanNameELResolver;
import jakarta.el.BeanNameResolver;
import jakarta.el.CompositeELResolver;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.jsp.el.ImplicitObjectELResolver;

public class NSFELResolver extends CompositeELResolver {
	public static final NSFELResolver instance = new NSFELResolver();

	public NSFELResolver() {
		add(new ImplicitObjectELResolver());
		add(new BeanNameELResolver(new CDIBeanResolver()));
	}
	
	public static class CDIBeanResolver extends BeanNameResolver {
		@Override
		public boolean isNameResolved(String beanName) {
			return CDI.current().select(NamedLiteral.of(beanName)).isResolvable();
		}
		
		@Override
		public Object getBean(String beanName) {
			return CDI.current().select(NamedLiteral.of(beanName)).get();
		}
	}
}
