package org.openntf.xsp.jakartaee.nsfmodule.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class NSFJakartaURLConnection extends URLConnection {
	private final String nsfPath;
	private final String res;

	protected NSFJakartaURLConnection(URL url, String nsfPath, String res) {
		super(url);
		this.nsfPath = nsfPath;
		this.res = res;
	}

	@Override
	public void connect() throws IOException {
		// NOP
	}
	
	@Override
	public InputStream getInputStream() throws IOException {
		return NSFAccess.openStream(nsfPath, res);
	}

}
