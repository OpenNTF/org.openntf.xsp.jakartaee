package org.openntf.xsp.nosql.communication.driver.lsxbe.util;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Contains utility methods for working with files
 * 
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public enum FileUtil {
	;
	
	/**
	 * Returns an appropriate temp directory for the system. On Windows, this is
	 * equivalent to <code>System.getProperty("java.io.tmpdir")</code>. On
	 * Linux, however, since this seems to return the data directory in some
	 * cases, it uses <code>/tmp</code>.
	 *
	 * @return an appropriate temp directory for the system
	 */
	public static Path getTempDirectory() {
		String osName = AccessController.doPrivileged((PrivilegedAction<String>)() -> System.getProperty("os.name")); //$NON-NLS-1$
		if (osName.startsWith("Linux") || osName.startsWith("LINUX")) { //$NON-NLS-1$ //$NON-NLS-2$
			return Paths.get("/tmp"); //$NON-NLS-1$
		} else {
			String tempDir = AccessController.doPrivileged((PrivilegedAction<String>)() -> System.getProperty("java.io.tmpdir")); //$NON-NLS-1$
			return Paths.get(tempDir);
		}
	}
}
