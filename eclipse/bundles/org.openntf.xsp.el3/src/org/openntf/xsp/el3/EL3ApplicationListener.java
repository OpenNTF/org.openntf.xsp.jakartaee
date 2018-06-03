package org.openntf.xsp.el3;

import org.openntf.xsp.jakartaee.LibraryUtil;

import com.ibm.xsp.application.ApplicationEx;
import com.ibm.xsp.application.events.ApplicationListener2;
import com.ibm.xsp.factory.FactoryLookup;

public class EL3ApplicationListener implements ApplicationListener2 {

	@Override
	public void applicationCreated(ApplicationEx application) {
		if(LibraryUtil.usesLibrary(EL3Library.LIBRARY_ID, application)) {
			@SuppressWarnings("deprecation")
			FactoryLookup facts = application.getFactoryLookup();
			facts.setFactory(EL3BindingFactory.PREFIX, new EL3BindingFactory());
		}
	}

	@Override
	public void applicationDestroyed(ApplicationEx application) {
		
	}

	@Override
	public void applicationRefreshed(ApplicationEx application) {
		
	}

}
