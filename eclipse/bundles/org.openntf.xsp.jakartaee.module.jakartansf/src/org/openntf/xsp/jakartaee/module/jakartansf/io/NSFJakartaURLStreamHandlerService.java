package org.openntf.xsp.jakartaee.module.jakartansf.io;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.osgi.service.url.AbstractURLStreamHandlerService;

public class NSFJakartaURLStreamHandlerService extends AbstractURLStreamHandlerService {

	@Override
	public URLConnection openConnection(URL u) throws IOException {
		return new NSFJakartaURLConnection(u);
	}

}
