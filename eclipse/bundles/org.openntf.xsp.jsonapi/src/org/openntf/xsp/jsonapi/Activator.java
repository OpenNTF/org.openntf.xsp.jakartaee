package org.openntf.xsp.jsonapi;

import org.eclipse.core.runtime.Plugin;

public class Activator extends Plugin {
	private static Activator instance;
	
	public static Activator getDefault() {
		return instance;
	}
	
	public Activator() {
		instance = this;
	}

}
