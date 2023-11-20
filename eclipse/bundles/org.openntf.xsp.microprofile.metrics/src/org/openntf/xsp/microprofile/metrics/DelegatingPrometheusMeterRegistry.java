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

import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;
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

	public DelegatingPrometheusMeterRegistry(PrometheusConfig config) {
		super(config);
	}

	public int hashCode() {
		return getDelegate().hashCode();
	}

	public String scrape() {
		return getDelegate().scrape();
	}

	public String scrape(String contentType) {
		return getDelegate().scrape(contentType);
	}

	public boolean equals(Object obj) {
		return getDelegate().equals(obj);
	}

	public void scrape(Writer writer) throws IOException {
		getDelegate().scrape(writer);
	}

	public void scrape(Writer writer, String contentType) throws IOException {
		getDelegate().scrape(writer, contentType);
	}

	public String scrape(String contentType, Set<String> includedNames) {
		return getDelegate().scrape(contentType, includedNames);
	}

	public void scrape(Writer writer, String contentType, Set<String> includedNames) throws IOException {
		getDelegate().scrape(writer, contentType, includedNames);
	}

	public Counter newCounter(Id id) {
		return getDelegate().newCounter(id);
	}

	public DistributionSummary newDistributionSummary(Id id, DistributionStatisticConfig distributionStatisticConfig,
			double scale) {
		return getDelegate().newDistributionSummary(id, distributionStatisticConfig, scale);
	}

	public String toString() {
		return getDelegate().toString();
	}

	public List<Meter> getMeters() {
		return getDelegate().getMeters();
	}

	public void forEachMeter(Consumer<? super Meter> consumer) {
		getDelegate().forEachMeter(consumer);
	}

	public Config config() {
		return getDelegate().config();
	}

	public Search find(String name) {
		return getDelegate().find(name);
	}

	public RequiredSearch get(String name) {
		return getDelegate().get(name);
	}

	public Counter counter(String name, Iterable<Tag> tags) {
		return getDelegate().counter(name, tags);
	}

	public Counter counter(String name, String... tags) {
		return getDelegate().counter(name, tags);
	}

	public DistributionSummary summary(String name, Iterable<Tag> tags) {
		return getDelegate().summary(name, tags);
	}

	public DistributionSummary summary(String name, String... tags) {
		return getDelegate().summary(name, tags);
	}

	public Timer timer(String name, Iterable<Tag> tags) {
		return getDelegate().timer(name, tags);
	}

	public Timer timer(String name, String... tags) {
		return getDelegate().timer(name, tags);
	}

	public More more() {
		return getDelegate().more();
	}

	public <T> T gauge(String name, Iterable<Tag> tags, T stateObject, ToDoubleFunction<T> valueFunction) {
		return getDelegate().gauge(name, tags, stateObject, valueFunction);
	}

	public CollectorRegistry getPrometheusRegistry() {
		return getDelegate().getPrometheusRegistry();
	}

	public <T extends Number> T gauge(String name, Iterable<Tag> tags, T number) {
		return getDelegate().gauge(name, tags, number);
	}

	public <T extends Number> T gauge(String name, T number) {
		return getDelegate().gauge(name, number);
	}

	public <T> T gauge(String name, T stateObject, ToDoubleFunction<T> valueFunction) {
		return getDelegate().gauge(name, stateObject, valueFunction);
	}

	public <T extends Collection<?>> T gaugeCollectionSize(String name, Iterable<Tag> tags, T collection) {
		return getDelegate().gaugeCollectionSize(name, tags, collection);
	}

	public <T extends Map<?, ?>> T gaugeMapSize(String name, Iterable<Tag> tags, T map) {
		return getDelegate().gaugeMapSize(name, tags, map);
	}

	public Meter remove(Meter meter) {
		return getDelegate().remove(meter);
	}

	public PrometheusMeterRegistry throwExceptionOnRegistrationFailure() {
		return getDelegate().throwExceptionOnRegistrationFailure();
	}

	public Meter removeByPreFilterId(Id preFilterId) {
		return getDelegate().removeByPreFilterId(preFilterId);
	}

	public Meter remove(Id mappedId) {
		return getDelegate().remove(mappedId);
	}

	public void clear() {
		getDelegate().clear();
	}

	public void close() {
		getDelegate().close();
	}

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
