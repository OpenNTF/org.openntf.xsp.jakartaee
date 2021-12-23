package org.openntf.xsp.microprofile.openapi.config;

import org.eclipse.microprofile.config.ConfigValue;

public class NOPConfigValue implements ConfigValue {
	public static final NOPConfigValue instance = new NOPConfigValue();

	@Override
	public String getName() {
		return null;
	}

	@Override
	public String getValue() {
		return null;
	}

	@Override
	public String getRawValue() {
		return null;
	}

	@Override
	public String getSourceName() {
		return null;
	}

	@Override
	public int getSourceOrdinal() {
		return 0;
	}

}
