package org.openntf.xsp.el3.ext;

import java.util.Collection;

import javax.el.ELResolver;

/**
 * Service extension point to allow client code to provide custom {@link ELResolver}
 * implementations to be added to the XPages replacement resolver.
 * 
 * @author Jesse Gallagher
 * @since 1.2.0
 */
@FunctionalInterface
public interface ELResolverProvider {
	Collection<ELResolver> provide();
}
