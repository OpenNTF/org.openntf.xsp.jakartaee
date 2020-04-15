package org.openntf.xsp.cdi.provider;

import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.CDIProvider;

import org.openntf.xsp.cdi.util.ContainerUtil;

import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.domino.osgi.core.context.ContextInfo;

public class OSGiServletCDIProvider implements CDIProvider {

	@Override
	public CDI<Object> getCDI() {
		try {
			NotesDatabase database = ContextInfo.getServerDatabase();
			if(database != null) {
				return ContainerUtil.getContainer(database);
			}
		} catch(Throwable t) {
			t.printStackTrace();
		}
		return null;
	}
	
	@Override
	public int getPriority() {
		return DEFAULT_CDI_PROVIDER_PRIORITY+1;
	}

}
