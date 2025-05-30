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
