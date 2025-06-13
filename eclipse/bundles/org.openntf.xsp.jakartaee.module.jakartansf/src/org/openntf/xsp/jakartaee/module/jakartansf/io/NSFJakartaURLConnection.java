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
package org.openntf.xsp.jakartaee.module.jakartansf.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;

import org.openntf.xsp.jakartaee.module.jakartansf.NSFJakartaModule;
import org.openntf.xsp.jakartaee.module.jakartansf.NSFJakartaModuleService;

public class NSFJakartaURLConnection extends URLConnection {

	protected NSFJakartaURLConnection(URL url) {
		super(url);
	}

	@Override
	public void connect() throws IOException {
		// NOP
	}
	
	@Override
	public InputStream getInputStream() throws IOException {
		// Path format is "/someapp!/some/resource"
		String path = url.getPath();
		int bangIndex = path.indexOf('!');
		if(bangIndex < 0) {
			throw new IllegalStateException(MessageFormat.format("Missing resource delimiter in URL {0}", url));
		}
		
		String mappingPath = path.substring(1, bangIndex);
		NSFJakartaModuleService service = NSFJakartaModuleService.getInstance(null);
		
		// Now find our module
		NSFJakartaModule module = service.getModule(mappingPath)
			.orElseThrow(() -> new IllegalStateException(MessageFormat.format("Could not locate {0} for path {1}", NSFJakartaModule.class.getName(), mappingPath)));
		
		// The raw query is the URL-encoded resource path
		String res = path.substring(bangIndex+1);
		
		return module.getRuntimeFileSystem().openStream(res)
			.orElseThrow(() -> new IllegalStateException(MessageFormat.format("Could not open resource \"{0}\" in module {1}", res, module)));
	}
}
