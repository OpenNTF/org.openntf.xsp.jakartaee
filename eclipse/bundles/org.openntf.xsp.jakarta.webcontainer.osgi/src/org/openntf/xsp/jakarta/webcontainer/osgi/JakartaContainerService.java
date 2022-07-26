package org.openntf.xsp.jakarta.webcontainer.osgi;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.HttpService;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletRequestAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpServletResponseAdapter;
import com.ibm.designer.runtime.domino.bootstrap.adapter.HttpSessionAdapter;

/**
 * 
 * @author Jesse Gallagher
 * @since 2.8.0
 */
public class JakartaContainerService extends HttpService {
	private static final Logger log = Logger.getLogger(JakartaContainerService.class.getPackage().getName());
	
	private Map<String, JakartaContainerModule> modules = new HashMap<>();

	public JakartaContainerService(LCDEnvironment env) {
		super(env);
		
		if(log.isLoggable(Level.INFO)) {
			log.info(getClass().getSimpleName() + " init"); //$NON-NLS-1$
		}
		
		IExtensionRegistry reg = Platform.getExtensionRegistry();
        if (reg != null) {
        	String serviceType = getClass().getPackage().getName() + ".application"; //$NON-NLS-1$
        	IExtensionPoint point = reg.getExtensionPoint(serviceType);
        	IExtension[] extensions = point.getExtensions();
            for(IExtension ext : extensions) {
            	String bundleName = ext.getContributor().getName();
            	
            	Bundle bundle = Platform.getBundle(bundleName);
            	String contextRoot = Arrays.stream(ext.getConfigurationElements())
            		.filter(e -> "contextRoot".equals(e.getName())) //$NON-NLS-1$
            		.findFirst()
            		.map(IConfigurationElement::getValue)
            		.orElse(null);
            	if(StringUtil.isEmpty(contextRoot)) {
            		throw new IllegalStateException(MessageFormat.format("Extension from {0} must include a contextRoot", bundleName));
            	}
            	if(!contextRoot.startsWith("/")) { //$NON-NLS-1$
            		contextRoot = "/" + contextRoot; //$NON-NLS-1$
            	}
            	Optional<String> contentLocation = Arrays.stream(ext.getConfigurationElements())
            		.filter(e -> "contentLocation".equals(e.getName())) //$NON-NLS-1$
            		.findFirst()
            		.map(IConfigurationElement::getValue);
            	
            	modules.put(contextRoot, new JakartaContainerModule(env, this, bundle, contextRoot, contentLocation.orElse(null)));
            }
        }
        
        // TODO initialize modules on another thread
	}

	@Override
	public void getModules(List<ComponentModule> modules) {
		modules.addAll(this.modules.values());
	}

	@Override
	public boolean doService(String contextPath, String path, HttpSessionAdapter httpSession, HttpServletRequestAdapter httpRequest,
			HttpServletResponseAdapter httpResponse) throws ServletException, IOException {
		Map.Entry<String, JakartaContainerModule> module = findModule(path);
		if(module != null) {
			String contextRoot = module.getKey();
			String moduleContextPath = path.substring(0, contextRoot.length());
			String pathInfo = path.substring(contextRoot.length());
			
			module.getValue().doService(moduleContextPath, pathInfo, httpSession, httpRequest, httpResponse);
			
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isXspUrl(String fullPath, boolean arg1) {
		if(findModule(fullPath) != null) {
			return true;
		}
		return false;
	}
	
	@Override
	public void checkTimeout(long timeout) {
		this.modules.values().forEach(m -> m.checkTimeout(timeout));
	}
	
	// *******************************************************************************
	// * Internal implementation utilities
	// *******************************************************************************
	
	private Map.Entry<String, JakartaContainerModule> findModule(String fullPath) {
		String path = StringUtil.toString(fullPath);
		int qIndex = path.indexOf('?');
		if(qIndex > -1) {
			path = path.substring(0, qIndex);
		}
		for(Map.Entry<String, JakartaContainerModule> entry : this.modules.entrySet()) {
			String contextRoot = entry.getKey();
			if(contextRoot.equals(path) || path.startsWith(contextRoot + "/")) { //$NON-NLS-1$
				return entry;
			}
		}
		return null;
	}

}
