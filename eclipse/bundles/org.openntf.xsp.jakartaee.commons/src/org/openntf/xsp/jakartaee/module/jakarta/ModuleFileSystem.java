package org.openntf.xsp.jakartaee.module.jakarta;

import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @since 3.5.0
 */
public interface ModuleFileSystem {
	Optional<URL> getUrl(String res);
	
	Optional<InputStream> openStream(String res);
	
	Stream<String> listFiles();
	
	Stream<String> listFiles(String basePath);
}