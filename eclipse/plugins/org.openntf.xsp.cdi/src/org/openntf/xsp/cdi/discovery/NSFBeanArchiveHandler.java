package org.openntf.xsp.cdi.discovery;

import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.Priority;

import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.environment.deployment.discovery.BeanArchiveBuilder;
import org.jboss.weld.environment.deployment.discovery.BeanArchiveHandler;

import com.ibm.domino.xsp.module.nsf.NSFComponentModule;
import com.ibm.domino.xsp.module.nsf.NotesContext;

@Priority(Integer.MAX_VALUE)
public class NSFBeanArchiveHandler implements BeanArchiveHandler {

	public NSFBeanArchiveHandler() {
	}

	private static final String PREFIX_CLASSES = "WEB-INF/classes/"; //$NON-NLS-1$
	private static final String SUFFIX_CLASS = ".class"; //$NON-NLS-1$
	private static final Pattern IGNORE_CLASSES = Pattern.compile("^(xsp|plugin)\\..*$"); //$NON-NLS-1$

	@Override
	public BeanArchiveBuilder handle(String beanArchiveReference) {
		NotesContext context = NotesContext.getCurrent();
		NSFComponentModule module = context.getModule();
		// Slightly customize the builder to keep some extra metadata
		BeanArchiveBuilder builder = new BeanArchiveBuilder() {
			{
				super.setBeansXml(BeansXml.EMPTY_BEANS_XML);
				super.setId(module.getDatabasePath());
			}
			
			@Override
			public BeanArchiveBuilder setBeansXml(BeansXml beansXml) {
				return this;
			}
		};
		
		module.getRuntimeFileSystem().getAllResources().entrySet().stream()
			.map(Map.Entry::getKey)
			.filter(key -> key.startsWith(PREFIX_CLASSES) && key.endsWith(SUFFIX_CLASS))
			.map(key -> key.substring(PREFIX_CLASSES.length(), key.length()-SUFFIX_CLASS.length()))
			.map(key -> key.replace('/', '.'))
			.filter(className -> !IGNORE_CLASSES.matcher(className).matches())
			.forEach(builder::addClass);
		
		return builder;
	}

}
