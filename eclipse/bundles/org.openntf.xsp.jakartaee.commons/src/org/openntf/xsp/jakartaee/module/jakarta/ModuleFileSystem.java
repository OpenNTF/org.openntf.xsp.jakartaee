package org.openntf.xsp.jakartaee.module.jakarta;

import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @since 3.5.0
 */
public interface ModuleFileSystem {
	public record FileEntry(String name, Object metadata) {}
	
	Optional<URL> getUrl(String res);

	Optional<URL> getWebResourceUrl(String res);
	
	Optional<InputStream> openStream(String res);
	
	Stream<FileEntry> listFiles();
	
	Stream<FileEntry> listFiles(String basePath);
}