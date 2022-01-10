package org.openntf.xsp.microprofile.config.sources;

import java.util.Collections;
import java.util.Set;

import org.eclipse.microprofile.config.spi.ConfigSource;

import com.ibm.domino.napi.NException;
import com.ibm.domino.napi.c.Os;

/**
 * {@link ConfigSource} implementation that reads properties from the
 * active Notes environment's notes.ini values.
 * 
 * @author Jesse Gallagher
 * @since 2.2.0
 */
public class NotesEnvironmentConfigSource implements ConfigSource {

	@Override
	public String getName() {
		return "NotesEnvironment"; //$NON-NLS-1$
	}

	@Override
	public Set<String> getPropertyNames() {
		return Collections.emptySet();
	}

	@Override
	public String getValue(String propertyName) {
		try {
			return Os.OSGetEnvironmentString(propertyName);
		} catch (NException e) {
			throw new RuntimeException(e);
		}
	}

}
