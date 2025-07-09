/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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