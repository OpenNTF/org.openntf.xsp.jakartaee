package org.openntf.xsp.jakartaee.servlet;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.servlet.ServletRegistration;

/**
 * @since 3.2.0
 */
public class DummyServletRegistration implements ServletRegistration {
	private final String name;
	private final String servletClassName;
	private final Set<String> extensions;
	
	public DummyServletRegistration(String name, String servletClassName, Set<String> extensions) {
		this.name = name;
		this.servletClassName = servletClassName;
		this.extensions = extensions;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getClassName() {
		return servletClassName;
	}

	@Override
	public boolean setInitParameter(String name, String value) {
		// NOP
		return false;
	}

	@Override
	public String getInitParameter(String name) {
		// NOP
		return null;
	}

	@Override
	public Set<String> setInitParameters(Map<String, String> initParameters) {
		// NOP
		return Collections.emptySet();
	}

	@Override
	public Map<String, String> getInitParameters() {
		return Collections.emptyMap();
	}

	@Override
	public Set<String> addMapping(String... urlPatterns) {
		// NOP
		return this.extensions;
	}

	@Override
	public Collection<String> getMappings() {
		return extensions.stream()
				.map(ext -> "*" + ext) //$NON-NLS-1$
				.collect(Collectors.toSet());
	}

	@Override
	public String getRunAsRole() {
		return null;
	}

}
