package org.openntf.xsp.microprofile.metrics;

import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.microprofile.metrics.ConcurrentGauge;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.Meter;
import org.eclipse.microprofile.metrics.Metric;
import org.eclipse.microprofile.metrics.MetricFilter;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.SimpleTimer;
import org.eclipse.microprofile.metrics.Tag;
import org.eclipse.microprofile.metrics.Timer;
import org.openntf.xsp.jakartaee.module.ComponentModuleLocator;
import com.ibm.designer.runtime.domino.adapter.ComponentModule;

import io.smallrye.metrics.MemberToMetricMappings;
import io.smallrye.metrics.MetricsRegistryImpl;
import jakarta.servlet.ServletContext;

public class DelegatingMetricRegistry extends MetricsRegistryImpl {
	private static final String KEY_DELEGATE = DelegatingMetricRegistry.class.getName() + "_delegate"; //$NON-NLS-1$
	
	private final Type type;
	
	public DelegatingMetricRegistry(Type type) {
		this.type = type;
	}

	@Override
	public <T extends Metric> T register(String name, T metric) throws IllegalArgumentException {
		return getDelegate().register(name, metric);
	}

	@Override
	public <T extends Metric> T register(Metadata metadata, T metric) throws IllegalArgumentException {
		return getDelegate().register(metadata, metric);
	}

	@Override
	public <T extends Metric> T register(Metadata metadata, T metric, Tag... tags) throws IllegalArgumentException {
		return getDelegate().register(metadata, metric, tags);
	}

	@Override
	public Counter counter(String name) {
		return getDelegate().counter(name);
	}

	@Override
	public Counter counter(String name, Tag... tags) {
		return getDelegate().counter(name, tags);
	}

	@Override
	public Counter counter(MetricID metricID) {
		return getDelegate().counter(metricID);
	}

	@Override
	public Counter counter(Metadata metadata) {
		return getDelegate().counter(metadata);
	}

	@Override
	public Counter counter(Metadata metadata, Tag... tags) {
		return getDelegate().counter(metadata, tags);
	}

	@Override
	public ConcurrentGauge concurrentGauge(String name) {
		return getDelegate().concurrentGauge(name);
	}

	@Override
	public ConcurrentGauge concurrentGauge(String name, Tag... tags) {
		return getDelegate().concurrentGauge(name, tags);
	}

	@Override
	public ConcurrentGauge concurrentGauge(MetricID metricID) {
		return getDelegate().concurrentGauge(metricID);
	}

	@Override
	public ConcurrentGauge concurrentGauge(Metadata metadata) {
		return getDelegate().concurrentGauge(metadata);
	}

	@Override
	public ConcurrentGauge concurrentGauge(Metadata metadata, Tag... tags) {
		return getDelegate().concurrentGauge(metadata, tags);
	}

	@Override
	public <T, R extends Number> Gauge<R> gauge(String name, T object, Function<T, R> func, Tag... tags) {
		return getDelegate().gauge(name, object, func, tags);
	}

	@Override
	public <T, R extends Number> Gauge<R> gauge(MetricID metricID, T object, Function<T, R> func) {
		return getDelegate().gauge(metricID, object, func);
	}

	@Override
	public <T, R extends Number> Gauge<R> gauge(Metadata metadata, T object, Function<T, R> func, Tag... tags) {
		return getDelegate().gauge(metadata, object, func, tags);
	}

	@Override
	public <T extends Number> Gauge<T> gauge(String name, Supplier<T> supplier, Tag... tags) {
		return getDelegate().gauge(name, supplier, tags);
	}

	@Override
	public <T extends Number> Gauge<T> gauge(MetricID metricID, Supplier<T> supplier) {
		return getDelegate().gauge(metricID, supplier);
	}

	@Override
	public <T extends Number> Gauge<T> gauge(Metadata metadata, Supplier<T> supplier, Tag... tags) {
		return getDelegate().gauge(metadata, supplier, tags);
	}

	@Override
	public Histogram histogram(String name) {
		return getDelegate().histogram(name);
	}

	@Override
	public Histogram histogram(String name, Tag... tags) {
		return getDelegate().histogram(name, tags);
	}

	@Override
	public Histogram histogram(MetricID metricID) {
		return getDelegate().histogram(metricID);
	}

	@Override
	public Histogram histogram(Metadata metadata) {
		return getDelegate().histogram(metadata);
	}

	@Override
	public Histogram histogram(Metadata metadata, Tag... tags) {
		return getDelegate().histogram(metadata, tags);
	}

	@Override
	public Meter meter(String name) {
		return getDelegate().meter(name);
	}

	@Override
	public Meter meter(String name, Tag... tags) {
		return getDelegate().meter(name, tags);
	}

	@Override
	public Meter meter(MetricID metricID) {
		return getDelegate().meter(metricID);
	}

	@Override
	public Meter meter(Metadata metadata) {
		return getDelegate().meter(metadata);
	}

	@Override
	public Meter meter(Metadata metadata, Tag... tags) {
		return getDelegate().meter(metadata, tags);
	}

	@Override
	public Timer timer(String name) {
		return getDelegate().timer(name);
	}

	@Override
	public Timer timer(String name, Tag... tags) {
		return getDelegate().timer(name, tags);
	}

	@Override
	public Timer timer(MetricID metricID) {
		return getDelegate().timer(metricID);
	}

	@Override
	public Timer timer(Metadata metadata) {
		return getDelegate().timer(metadata);
	}

	@Override
	public Timer timer(Metadata metadata, Tag... tags) {
		return getDelegate().timer(metadata, tags);
	}

	@Override
	public SimpleTimer simpleTimer(String name) {
		return getDelegate().simpleTimer(name);
	}

	@Override
	public SimpleTimer simpleTimer(String name, Tag... tags) {
		return getDelegate().simpleTimer(name, tags);
	}

	@Override
	public SimpleTimer simpleTimer(MetricID metricID) {
		return getDelegate().simpleTimer(metricID);
	}

	@Override
	public SimpleTimer simpleTimer(Metadata metadata) {
		return getDelegate().simpleTimer(metadata);
	}

	@Override
	public SimpleTimer simpleTimer(Metadata metadata, Tag... tags) {
		return getDelegate().simpleTimer(metadata, tags);
	}

	@Override
	public Metric getMetric(MetricID metricID) {
		return getDelegate().getMetric(metricID);
	}

	@Override
	public <T extends Metric> T getMetric(MetricID metricID, Class<T> asType) {
		return getDelegate().getMetric(metricID, asType);
	}

	@Override
	public Counter getCounter(MetricID metricID) {
		return getDelegate().getCounter(metricID);
	}

	@Override
	public ConcurrentGauge getConcurrentGauge(MetricID metricID) {
		return getDelegate().getConcurrentGauge(metricID);
	}

	@Override
	public Gauge<?> getGauge(MetricID metricID) {
		return getDelegate().getGauge(metricID);
	}

	@Override
	public Histogram getHistogram(MetricID metricID) {
		return getDelegate().getHistogram(metricID);
	}

	@Override
	public Meter getMeter(MetricID metricID) {
		return getDelegate().getMeter(metricID);
	}

	@Override
	public Timer getTimer(MetricID metricID) {
		return getDelegate().getTimer(metricID);
	}

	@Override
	public SimpleTimer getSimpleTimer(MetricID metricID) {
		return getDelegate().getSimpleTimer(metricID);
	}

	@Override
	public Metadata getMetadata(String name) {
		return getDelegate().getMetadata(name);
	}

	@Override
	public boolean remove(String name) {
		return getDelegate().remove(name);
	}

	@Override
	public boolean remove(MetricID metricID) {
		return getDelegate().remove(metricID);
	}

	@Override
	public void removeMatching(MetricFilter filter) {
		getDelegate().removeMatching(filter);
	}

	@Override
	public SortedSet<String> getNames() {
		return getDelegate().getNames();
	}

	@Override
	public SortedSet<MetricID> getMetricIDs() {
		return getDelegate().getMetricIDs();
	}

	@Override
	public SortedMap<MetricID, Gauge> getGauges() {
		return getDelegate().getGauges();
	}

	@Override
	public SortedMap<MetricID, Gauge> getGauges(MetricFilter filter) {
		return getDelegate().getGauges(filter);
	}

	@Override
	public SortedMap<MetricID, Counter> getCounters() {
		return getDelegate().getCounters();
	}

	@Override
	public SortedMap<MetricID, Counter> getCounters(MetricFilter filter) {
		return getDelegate().getCounters(filter);
	}

	@Override
	public SortedMap<MetricID, ConcurrentGauge> getConcurrentGauges() {
		return getDelegate().getConcurrentGauges();
	}

	@Override
	public SortedMap<MetricID, ConcurrentGauge> getConcurrentGauges(MetricFilter filter) {
		return getDelegate().getConcurrentGauges(filter);
	}

	@Override
	public SortedMap<MetricID, Histogram> getHistograms() {
		return getDelegate().getHistograms();
	}

	@Override
	public SortedMap<MetricID, Histogram> getHistograms(MetricFilter filter) {
		return getDelegate().getHistograms(filter);
	}

	@Override
	public SortedMap<MetricID, Meter> getMeters() {
		return getDelegate().getMeters();
	}

	@Override
	public SortedMap<MetricID, Meter> getMeters(MetricFilter filter) {
		return getDelegate().getMeters(filter);
	}

	@Override
	public SortedMap<MetricID, Timer> getTimers() {
		return getDelegate().getTimers();
	}

	@Override
	public SortedMap<MetricID, Timer> getTimers(MetricFilter filter) {
		return getDelegate().getTimers(filter);
	}

	@Override
	public SortedMap<MetricID, SimpleTimer> getSimpleTimers() {
		return getDelegate().getSimpleTimers();
	}

	@Override
	public SortedMap<MetricID, SimpleTimer> getSimpleTimers(MetricFilter filter) {
		return getDelegate().getSimpleTimers(filter);
	}

	@Override
	public SortedMap<MetricID, Metric> getMetrics(MetricFilter filter) {
		return getDelegate().getMetrics(filter);
	}

	@Override
	public <T extends Metric> SortedMap<MetricID, T> getMetrics(Class<T> ofType, MetricFilter filter) {
		return getDelegate().getMetrics(ofType, filter);
	}

	@Override
	public Map<MetricID, Metric> getMetrics() {
		return getDelegate().getMetrics();
	}

	@Override
	public Map<String, Metadata> getMetadata() {
		return getDelegate().getMetadata();
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public MemberToMetricMappings getMemberToMetricMappings() {
		return getDelegate().getMemberToMetricMappings();
	}

	private MetricsRegistryImpl getDelegate() {
		Optional<ServletContext> ctx = ComponentModuleLocator.getDefault()
			.flatMap(ComponentModuleLocator::getServletContext);
		if(ctx.isPresent()) {
			MetricsRegistryImpl del = (MetricsRegistryImpl)ctx.get().getAttribute(KEY_DELEGATE);
			if(del == null) {
				del = new MetricsRegistryImpl(type);
				ctx.get().setAttribute(KEY_DELEGATE, del);
			}
			return del;
		}
		
		Optional<ComponentModule> mod = ComponentModuleLocator.getDefault()
			.map(ComponentModuleLocator::getActiveModule);
		if(mod.isPresent()) {
			MetricsRegistryImpl del = (MetricsRegistryImpl)mod.get().getAttributes().get(KEY_DELEGATE);
			if(del == null) {
				del = new MetricsRegistryImpl(type);
				mod.get().getAttributes().put(KEY_DELEGATE, del);
			}
			return del;
		}
		
		return new MetricsRegistryImpl(type);
	}
}
