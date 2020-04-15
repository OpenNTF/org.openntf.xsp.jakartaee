package org.openntf.xsp.jsp.nsf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Dictionary;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import org.eclipse.osgi.container.Module.Settings;
import org.eclipse.osgi.internal.framework.EquinoxBundle;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.Version;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;

/**
 * Faux {@link Bundle} implementation that delegates applicable calls to an underlying
 * {@link ComponentModule}.
 * 
 * @author Jesse Gallagher
 * @since 1.2.0
 */
@SuppressWarnings("restriction")
public class ComponentModuleBundle extends EquinoxBundle {
	private final ComponentModule module;
	
	public ComponentModuleBundle(ComponentModule module) {
		super(-1l, null, null, EnumSet.noneOf(Settings.class), -1, null);
		this.module = module;
	}

	@Override
	public URL getResource(String name) {
		URL res = module.getModuleClassLoader().getResource(name);
		if(res != null) {
			return res;
		}
		return getClass().getResource(name);
	}

	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		Enumeration<URL> resources = module.getModuleClassLoader().getResources(name);
		if(resources != null && resources.hasMoreElements()) {
			return resources;
		}
		return getClass().getClassLoader().getResources(name);
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		Class<?> clazz = module.getModuleClassLoader().loadClass(name);
		if(clazz != null) {
			return null;
		}
		return getClass().getClassLoader().loadClass(name);
	}

	@Override
	public Dictionary<String, String> getHeaders() {
		// TODO add WEB-INF/lib jars to Bundle-ClassPath
		return new Hashtable<>();
	}

	@Override
	public boolean hasPermission(Object permission) {
		return true;
	}
	
	// *******************************************************************************
	// * Stubs
	// *******************************************************************************


	@Override
	public Dictionary<String, String> getHeaders(String arg0) {
		return null;
	}
	
	@Override
	public int compareTo(Bundle o) {
		return 0;
	}

	@Override
	public Enumeration<URL> findEntries(String path, String filePattern, boolean recurse) {
		return null;
	}

	@Override
	public BundleContext getBundleContext() {
		return null;
	}

	@Override
	public long getBundleId() {
		return 0;
	}

	@Override
	public File getDataFile(String arg0) {
		return null;
	}

	@Override
	public URL getEntry(String arg0) {
		return null;
	}

	@Override
	public Enumeration<String> getEntryPaths(String arg0) {
		return null;
	}

	@Override
	public long getLastModified() {
		return 0;
	}

	@Override
	public String getLocation() {
		return null;
	}

	@Override
	public ServiceReference<?>[] getRegisteredServices() {
		return null;
	}

	@Override
	public ServiceReference<?>[] getServicesInUse() {
		return null;
	}

	@Override
	public Map<X509Certificate, List<X509Certificate>> getSignerCertificates(int arg0) {
		return null;
	}

	@Override
	public int getState() {
		return 0;
	}

	@Override
	public String getSymbolicName() {
		return null;
	}

	@Override
	public Version getVersion() {
		return null;
	}

	@Override
	public void start() throws BundleException {
	}

	@Override
	public void start(int arg0) throws BundleException {
	}

	@Override
	public void stop() throws BundleException {
	}

	@Override
	public void stop(int arg0) throws BundleException {
	}

	@Override
	public void uninstall() throws BundleException {
	}

	@Override
	public void update() throws BundleException {
	}

	@Override
	public void update(InputStream arg0) throws BundleException {
	}

}
