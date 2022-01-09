package org.openntf.xsp.microprofile.metrics.jaxrs;

import java.io.IOException;
import java.time.Duration;

import io.smallrye.metrics.MetricRegistries;
import io.smallrye.metrics.jaxrs.JaxRsMetricsServletFilter;

import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.openntf.xsp.jaxrs.ServiceParticipant;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * This participant performs a similar job as {@link JaxRsMetricsServletFilter},
 * but works around Domino's lack of Filter support.
 * 
 * @author Jesse Gallagher
 * @since 2.3.0
 */
public class MetricsServiceParticipant implements ServiceParticipant {
	public static String ATTR_START = MetricsServiceParticipant.class + "_start"; //$NON-NLS-1$

	@Override
	public void doBeforeService(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		request.setAttribute(ATTR_START, System.nanoTime());
	}

	@Override
	public void doAfterService(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		MetricID metricID = (MetricID) request.getAttribute("smallrye.metrics.jaxrs.metricID"); //$NON-NLS-1$
        if (metricID != null) {
            createMetrics(metricID);
            boolean success = request.getAttribute("smallrye.metrics.jaxrs.successful") != null; //$NON-NLS-1$
            long start = (Long)request.getAttribute(ATTR_START);
            update(success, start, metricID);
        }
	}

    private void update(boolean success, long startTimestamp, MetricID metricID) {
        if (success) {
            updateAfterSuccess(startTimestamp, metricID);
        } else {
            updateAfterFailure(metricID);
        }
    }

    private void updateAfterSuccess(long startTimestamp, MetricID metricID) {
        long duration = System.nanoTime() - startTimestamp;
        MetricRegistry registry = MetricRegistries.get(MetricRegistry.Type.BASE);
        registry.getSimpleTimer(metricID).update(Duration.ofNanos(duration));
    }

    private void updateAfterFailure(MetricID metricID) {
        MetricRegistry registry = MetricRegistries.get(MetricRegistry.Type.BASE);
        registry.getCounter(transformToMetricIDForFailedRequest(metricID)).inc();
    }

    private MetricID transformToMetricIDForFailedRequest(MetricID metricID) {
        return new MetricID("REST.request.unmappedException.total", metricID.getTagsAsArray());
    }

    private void createMetrics(MetricID metricID) {
        MetricRegistry registry = MetricRegistries.get(MetricRegistry.Type.BASE);
        if (registry.getSimpleTimer(metricID) == null) {
            Metadata successMetadata = Metadata.builder()
                    .withName(metricID.getName())
                    .withDescription(
                            "The number of invocations and total response time of this RESTful " +
                                    "resource method since the start of the server.")
                    .withUnit(MetricUnits.NANOSECONDS)
                    .build();
            registry.simpleTimer(successMetadata, metricID.getTagsAsArray());
        }
        MetricID metricIDForFailure = transformToMetricIDForFailedRequest(metricID);
        if (registry.getCounter(metricIDForFailure) == null) {
            Metadata failureMetadata = Metadata.builder()
                    .withName(metricIDForFailure.getName())
                    .withDisplayName("Total Unmapped Exceptions count")
                    .withDescription(
                            "The total number of unmapped exceptions that occurred from this RESTful resource " +
                                    "method since the start of the server.")
                    .build();
            registry.counter(failureMetadata, metricIDForFailure.getTagsAsArray());
        }
    }

}
