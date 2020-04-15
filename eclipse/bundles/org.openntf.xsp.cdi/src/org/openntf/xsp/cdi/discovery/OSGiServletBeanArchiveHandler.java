package org.openntf.xsp.cdi.discovery;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.napi.NotesAPIException;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.domino.osgi.core.context.ContextInfo;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Priority;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.ManifestElement;
import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.environment.deployment.discovery.BeanArchiveBuilder;
import org.jboss.weld.environment.deployment.discovery.BeanArchiveHandler;
import org.openntf.xsp.cdi.util.ContainerUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.wiring.BundleWiring;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.2.0
 */
@Priority(Integer.MAX_VALUE-1)
public class OSGiServletBeanArchiveHandler implements BeanArchiveHandler {
	public static final ThreadLocal<Bundle> PROCESSING_BUNDLE = ThreadLocal.withInitial(() -> null);

	@Override
	public BeanArchiveBuilder handle(String beanArchiveReference) {
		try {
			Bundle bundle = PROCESSING_BUNDLE.get();
			if(bundle == null) {
				NotesDatabase database = ContextInfo.getServerDatabase();
				if(database != null) {
					String bundleName = ContainerUtil.getApplicationCDIBundle(database);
					bundle = Platform.getBundle(bundleName);
				}
			}
			
			if(bundle != null) {
				String symbolicName = bundle.getSymbolicName();
				// Slightly customize the builder to keep some extra metadata
				BeanArchiveBuilder builder = new BeanArchiveBuilder() {
					{
						super.setBeansXml(BeansXml.EMPTY_BEANS_XML);
						super.setId(symbolicName);
					}
					
					@Override
					public BeanArchiveBuilder setBeansXml(BeansXml beansXml) {
						return this;
					}
				};
				
				Collection<String> bundleNames = new HashSet<>();
				addClasses(bundle, builder, bundleNames);
				
				return builder;
			}
		} catch (NotesAPIException | IOException | BundleException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static void addClasses(Bundle bundle, BeanArchiveBuilder builder, Collection<String> bundleNames) throws BundleException {
		if(bundleNames.contains(bundle.getSymbolicName())) {
			return;
		}
		bundleNames.add(bundle.getSymbolicName());
		
		// Only look when there's a beans.xml, to be less costly
		if(bundle.getResource("/META-INF/beans.xml") != null || bundle.getResource("/WEB-INF/beans.xml") != null) { //$NON-NLS-1$ //$NON-NLS-2$
			String exportPackages = bundle.getHeaders().get("Export-Package"); //$NON-NLS-1$
			if(StringUtil.isNotEmpty(exportPackages)) {
				// Restrict to exported packages for sanity's sake
				ManifestElement[] elements = ManifestElement.parseHeader("Export-Package", exportPackages); //$NON-NLS-1$
				Set<String> packages = Arrays.stream(elements)
					.map(ManifestElement::getValue)
					.filter(StringUtil::isNotEmpty)
					.collect(Collectors.toSet());
				
				BundleWiring wiring = bundle.adapt(BundleWiring.class);
				wiring.listResources("/", "*.class", BundleWiring.LISTRESOURCES_RECURSE | BundleWiring.LISTRESOURCES_LOCAL).stream() //$NON-NLS-1$ //$NON-NLS-2$
					.parallel()
					.map(OSGiServletBeanArchiveHandler::toClassName)
					.filter(StringUtil::isNotEmpty)
					.filter(className -> packages.contains(className.substring(0, className.lastIndexOf('.'))))
					.forEach(builder::addClass);
			}
		}
		
		String requireBundle = bundle.getHeaders().get("Require-Bundle"); //$NON-NLS-1$
		if(StringUtil.isNotEmpty(requireBundle)) {
			ManifestElement[] elements = ManifestElement.parseHeader("Require-Bundle", requireBundle); //$NON-NLS-1$
			for(ManifestElement el : elements) {
				String bundleName = el.getValue();
				if(StringUtil.isNotEmpty(bundleName)) {
					Bundle dependency = Platform.getBundle(bundleName);
					if(dependency != null) {
						addClasses(dependency, builder, bundleNames);
					}
				}
			}
		}
	}
	
	private static String toClassName(String resourceName) {
		if(StringUtil.isEmpty(resourceName)) {
			return null;
		} else if(resourceName.startsWith("target/classes")) { //$NON-NLS-1$
			// Not a real class name
			return null;
		} else if(resourceName.startsWith("bin/")) { //$NON-NLS-1$
			// Not a real class name
			return null;
		}
		
		return resourceName
			.substring(0, resourceName.length()-".class".length()) //$NON-NLS-1$
			.replace('/', '.');
	}

}
