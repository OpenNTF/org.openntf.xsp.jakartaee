package org.openntf.xsp.jakartaee.module;

import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.stream.Stream;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.HttpService;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;

/**
 * Contains common implementation details for Jakarta Modules.
 * 
 * @since 3.5.0
 */
public abstract class AbstractJakartaModule extends ComponentModule {
	public interface ModuleFileSystem {
		Optional<URL> getUrl(String res);
		
		Optional<InputStream> openStream(String res);
		
		Stream<String> listFiles();
		
		Stream<String> listFiles(String basePath);
	}

	public AbstractJakartaModule(LCDEnvironment env, HttpService service, String name, boolean persistentSessions) {
		super(env, service, name, persistentSessions);
	}

	public abstract ModuleFileSystem getRuntimeFileSystem();
}
