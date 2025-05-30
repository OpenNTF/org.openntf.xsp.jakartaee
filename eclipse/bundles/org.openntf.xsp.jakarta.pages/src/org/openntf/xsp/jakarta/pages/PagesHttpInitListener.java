package org.openntf.xsp.jakarta.pages;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.commons.util.StringUtil;
import com.ibm.domino.napi.NException;
import com.ibm.domino.napi.c.Os;
import com.ibm.domino.xsp.module.nsf.NSFService;

import org.eclipse.core.runtime.FileLocator;
import org.glassfish.wasp.servlet.WaspLoader;
import org.openntf.xsp.jakartaee.events.JakartaHttpInitListener;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import jakarta.servlet.Servlet;

/**
 * @since 3.4.0
 */
public class PagesHttpInitListener implements JakartaHttpInitListener {
	/**
	 * notes.ini property that can be set to specify a DTD output directory.
	 * @since 3.1.0
	 */
	public static final String PROP_OVERRIDEDTDDIR = "Jakarta_DTDDir"; //$NON-NLS-1$

	private static final Logger log = Logger.getLogger(PagesHttpInitListener.class.getPackageName());
	
	@Override
	public void httpInit() throws Exception {
		initNsf();
		deployServletDtds();
	}
	
	/**
	 * Adds Jakarta Pages support to NSFs.
	 */
	private void initNsf() {
		// Register ".jsp" with the NSF service, which will then pass along to PagesServletFactory
		NSFService.addHandledExtensions(".jsp"); //$NON-NLS-1$

		// This property is used by Wasp to see if it should use AccessController blocks
		LibraryUtil.setSystemProperty("package.definition", "org.apache.jsp"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void deployServletDtds() throws URISyntaxException, IOException {
		Path destDir = getServletDtdPath();
		Files.createDirectories(destDir);

		Bundle servlet = FrameworkUtil.getBundle(Servlet.class);
		Enumeration<String> resources = servlet.getEntryPaths("/jakarta/servlet/resources/"); //$NON-NLS-1$
		for(String res : Collections.list(resources)) {
			URL url = servlet.getResource(res);

			String baseName = res.substring(res.lastIndexOf('/')+1);
			if(!baseName.isEmpty()) {
				Path dest = destDir.resolve(baseName);
				if(!Files.isRegularFile(dest)) {
					try(InputStream is = url.openStream()) {
						Files.copy(is, dest, StandardCopyOption.REPLACE_EXISTING);
					}
				}
			}
		}
	}

	public static Path getDeployedJstlBundle() throws IOException {
		Path destDir = getServletDtdPath();
		Files.createDirectories(destDir);

		Bundle jstl = FrameworkUtil.getBundle(WaspLoader.class);
		Path jstlDest = destDir.resolve(jstl.getSymbolicName() + "_" + jstl.getVersion() + ".jar"); //$NON-NLS-1$ //$NON-NLS-2$
		if(!Files.exists(jstlDest)) {
			Path jstlSource = FileLocator.getBundleFile(jstl).toPath();
			Files.copy(jstlSource, jstlDest);
		}

		return jstlDest;
	}

	public static Path getServletDtdPath() {
		try {
			String iniDtdDir = Os.OSGetEnvironmentString(PROP_OVERRIDEDTDDIR);
			if(StringUtil.isNotEmpty(iniDtdDir)) {
				Path dtdDir = Paths.get(iniDtdDir);
				if(!dtdDir.isAbsolute()) {
					Path dataDir = Paths.get(Os.OSGetDataDirectory());
					dtdDir = dataDir.resolve(iniDtdDir);
				}
				return dtdDir;
			}
		} catch(NException e) {
			if(log.isLoggable(Level.WARNING)) {
				log.log(Level.WARNING, "Encountered exception trying to read notes.ini", e);
			}
		}

		String data = Os.OSGetDataDirectory();
		Path dataDir = Paths.get(data);
		return dataDir.resolve("domino").resolve("jakarta").resolve("dtd"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
