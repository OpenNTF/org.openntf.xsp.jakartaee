package org.openntf.xsp.jakartaee.module.nsf.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * @since 3.4.0
 */
public enum NSFJakartaURL {
	;
	
	public static final String SCHEME = "jakartansf"; //$NON-NLS-1$
	
	public static URL of(String nsfPath, String path) {
		try {
			URI uri = new URI(SCHEME, null, "//" + path, null, null); //$NON-NLS-1$
			return URL.of(uri, new NSFJakartaURLStreamHandler(nsfPath, path));
		} catch(URISyntaxException e) {
			throw new RuntimeException(e);
		} catch (MalformedURLException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	private static class NSFJakartaURLStreamHandler extends URLStreamHandler {
		private final String nsfPath;
		private final String path;
		
		public NSFJakartaURLStreamHandler(String nsfPath, String path) {
			this.nsfPath = nsfPath;
			this.path = path;
		}

		@Override
		protected URLConnection openConnection(URL u) throws IOException {
			return NSFAccess.openConnection(u, nsfPath, path);
		}
		
	}
}
