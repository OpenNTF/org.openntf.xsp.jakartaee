package org.openntf.xsp.jakartaee.module.jakarta;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
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
	
	/**
	 * Builds a URI to the provided path, whether or not it exists in the module.
	 * 
	 * @param path the path to identify within the module
	 * @return a URI for the path
	 * @throws URISyntaxException if the URI cannot be built as specified
	 */
	URI buildURI(String path) throws URISyntaxException;
}