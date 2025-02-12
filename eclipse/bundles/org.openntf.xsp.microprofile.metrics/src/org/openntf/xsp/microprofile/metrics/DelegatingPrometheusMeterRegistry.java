/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openntf.xsp.microprofile.metrics;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.ToDoubleFunction;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;

import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Meter.Id;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig;
import io.micrometer.core.instrument.search.RequiredSearch;
import io.micrometer.core.instrument.search.Search;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import io.smallrye.metrics.MPPrometheusConfig;
import jakarta.servlet.ServletContext;

/**
 * This class delegates to an app-specific registry.
 *
 * @since 3.0.0
 */
public class DelegatingPrometheusMeterRegistry extends PrometheusMeterRegistry {
	private static final String KEY_DELEGATE = DelegatingPrometheusMeterRegistry.class.getName() + "_delegate"; //$NON-NLS-1$

	public DelegatingPrometheusMeterRegistry(final PrometheusConfig config) {
		super(config);
	}

	@Override
	public int hashCode() {
		return getDelegate().hashCode();
	}

	@Override
	public String scrape() {
		return getDelegate().scrape();
	}

	@Override
	public String scrape(final String contentType) {
		return getDelegate().scrape(contentType);
	}

	@Override
	public boolean equals(final Object obj) {
		return getDelegate().equals(obj);
	}

	@Override
	public void scrape(final Writer writer) throws IOException {
		getDelegate().scrape(writer);
	}

	@Override
	public void scrape(final Writer writer, final String contentType) throws IOException {
		getDelegate().scrape(writer, contentType);
	}

	@Override
	public String scrape(final String contentType, final Set<String> includedNames) {
		return getDelegate().scrape(contentType, includedNames);
	}

	@Override
	public void scrape(final Writer writer, final String contentType, final Set<String> includedNames) throws IOException {
		getDelegate().scrape(writer, contentType, includedNames);
	}

	@Override
	public Counter newCounter(final Id id) {
		return getDelegate().newCounter(id);
	}

	@Override
	public DistributionSummary newDistributionSummary(final Id id, final DistributionStatisticConfig distributionStatisticConfig,
			final double scale) {
		return getDelegate().newDistributionSummary(id, distributionStatisticConfig, scale);
	}

	@Override
	public String toString() {
		return getDelegate().toString();
	}

	@Override
	public List<Meter> getMeters() {
		return getDelegate().getMeters();
	}

	@Override
	public void forEachMeter(final Consumer<? super Meter> consumer) {
		getDelegate().forEachMeter(consumer);
	}

	@Override
	public Config config() {
		return getDelegate().config();
	}

	@Override
	public Search find(final String name) {
		return getDelegate().find(name);
	}

	@Override
	public RequiredSearch get(final String name) {
		return getDelegate().get(name);
	}

	@Override
	public Counter counter(final String name, final Iterable<Tag> tags) {
		return getDelegate().counter(name, tags);
	}

	@Override
	public Counter counter(final String name, final String... tags) {
		return getDelegate().counter(name, tags);
	}

	@Override
	public DistributionSummary summary(final String name, final Iterable<Tag> tags) {
		return getDelegate().summary(name, tags);
	}

	@Override
	public DistributionSummary summary(final String name, final String... tags) {
		return getDelegate().summary(name, tags);
	}

	@Override
	public Timer timer(final String name, final Iterable<Tag> tags) {
		return getDelegate().timer(name, tags);
	}

	@Override
	public Timer timer(final String name, final String... tags) {
		return getDelegate().timer(name, tags);
	}

	@Override
	public More more() {
		return getDelegate().more();
	}

	@Override
	public <T> T gauge(final String name, final Iterable<Tag> tags, final T stateObject, final ToDoubleFunction<T> valueFunction) {
		return getDelegate().gauge(name, tags, stateObject, valueFunction);
	}

	@Override
	public CollectorRegistry getPrometheusRegistry() {
		return getDelegate().getPrometheusRegistry();
	}

	@Override
	public <T extends Number> T gauge(final String name, final Iterable<Tag> tags, final T number) {
		return getDelegate().gauge(name, tags, number);
	}

	@Override
	public <T extends Number> T gauge(final String name, final T number) {
		return getDelegate().gauge(name, number);
	}

	@Override
	public <T> T gauge(final String name, final T stateObject, final ToDoubleFunction<T> valueFunction) {
		return getDelegate().gauge(name, stateObject, valueFunction);
	}

	@Override
	public <T extends Collection<?>> T gaugeCollectionSize(final String name, final Iterable<Tag> tags, final T collection) {
		return getDelegate().gaugeCollectionSize(name, tags, collection);
	}

	@Override
	public <T extends Map<?, ?>> T gaugeMapSize(final String name, final Iterable<Tag> tags, final T map) {
		return getDelegate().gaugeMapSize(name, tags, map);
	}

	@Override
	public Meter remove(final Meter meter) {
		return getDelegate().remove(meter);
	}

	@Override
	public PrometheusMeterRegistry throwExceptionOnRegistrationFailure() {
		return getDelegate().throwExceptionOnRegistrationFailure();
	}

	@Override
	public Meter removeByPreFilterId(final Id preFilterId) {
		return getDelegate().removeByPreFilterId(preFilterId);
	}

	@Override
	public Meter remove(final Id mappedId) {
		return getDelegate().remove(mappedId);
	}

	@Override
	public void clear() {
		getDelegate().clear();
	}

	@Override
	public void close() {
		getDelegate().close();
	}

	@Override
	public boolean isClosed() {
		return getDelegate().isClosed();
	}

	private PrometheusMeterRegistry getDelegate() {
		Optional<ServletContext> ctx = ComponentModuleLocator.getDefault()
			.flatMap(ComponentModuleLocator::getServletContext);
		if(ctx.isPresent()) {
			PrometheusMeterRegistry del = (PrometheusMeterRegistry)ctx.get().getAttribute(KEY_DELEGATE);
			if(del == null) {
				del = new PrometheusMeterRegistry(new MPPrometheusConfig());
				ctx.get().setAttribute(KEY_DELEGATE, del);
			}
			return del;
		}

		Optional<ComponentModule> mod = ComponentModuleLocator.getDefault()
			.map(ComponentModuleLocator::getActiveModule);
		if(mod.isPresent()) {
			PrometheusMeterRegistry del = (PrometheusMeterRegistry)mod.get().getAttributes().get(KEY_DELEGATE);
			if(del == null) {
				del = new PrometheusMeterRegistry(new MPPrometheusConfig());
				mod.get().getAttributes().put(KEY_DELEGATE, del);
			}
			return del;
		}

		return new PrometheusMeterRegistry(new MPPrometheusConfig());
	}
}
