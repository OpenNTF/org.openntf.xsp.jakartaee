package org.openntf.xsp.jakartaee.events.impl;

import java.util.List;

import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.application.events.ApplicationListener2;

import org.openntf.xsp.jakartaee.events.JakartaApplicationListener;
import org.openntf.xsp.jakartaee.util.LibraryUtil;

public class JakartaApplicationListenerImpl implements ApplicationListener2 {
	private List<JakartaApplicationListener> listeners;

	@Override
	public void applicationCreated(ApplicationEx application) {
		listeners = LibraryUtil.findExtensionsSorted(JakartaApplicationListener.class, false);
		
		listeners.forEach(l -> l.applicationCreated(application));
	}

	@Override
	public void applicationDestroyed(ApplicationEx application) {
		for(int i = listeners.size()-1; i >= 0; i--) {
			listeners.get(i).applicationDestroyed(application);
		}
	}

	@Override
	public void applicationRefreshed(ApplicationEx application) {
		listeners.forEach(l -> l.applicationRefreshed(application));
	}

}
