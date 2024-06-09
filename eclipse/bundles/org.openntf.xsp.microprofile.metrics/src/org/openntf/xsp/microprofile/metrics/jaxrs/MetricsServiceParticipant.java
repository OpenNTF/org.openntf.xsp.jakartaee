/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.microprofile.metrics.jaxrs;

import java.io.IOException;
import java.time.Duration;

import io.smallrye.metrics.SharedMetricRegistries;
import io.smallrye.metrics.jaxrs.JaxRsMetricsServletFilter;

import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.openntf.xsp.jakarta.rest.ServiceParticipant;

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
	public void doAfterService(HttpServletRequest servletRequest, HttpServletResponse response)
			throws ServletException, IOException {
		long start = (Long)servletRequest.getAttribute(ATTR_START);
		MetricID metricID = (MetricID) servletRequest.getAttribute("smallrye.metrics.jaxrs.metricID");
        if (metricID != null) {
            createMetrics(metricID);
            boolean success = servletRequest.getAttribute("smallrye.metrics.jaxrs.successful") != null;
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

    //TODO: Verify it works properly.
    private void updateAfterSuccess(long startTimestamp, MetricID metricID) {
        long duration = System.nanoTime() - startTimestamp;
        MetricRegistry registry = SharedMetricRegistries.getOrCreate(MetricRegistry.BASE_SCOPE);
        registry.getTimer(metricID).update(Duration.ofNanos(duration));
    }

    private void updateAfterFailure(MetricID metricID) {
        MetricRegistry registry = SharedMetricRegistries.getOrCreate(MetricRegistry.BASE_SCOPE);
        registry.getCounter(transformToMetricIDForFailedRequest(metricID)).inc();
    }

    private MetricID transformToMetricIDForFailedRequest(MetricID metricID) {
        return new MetricID("REST.request.unmappedException.total", metricID.getTagsAsArray());
    }

    //TODO: Verify it works properly.
    private void createMetrics(MetricID metricID) {
        MetricRegistry registry = SharedMetricRegistries.getOrCreate(MetricRegistry.BASE_SCOPE);
        if (registry.getTimer(metricID) == null) {
            Metadata successMetadata = Metadata.builder()
                    .withName(metricID.getName())
                    .withDescription(
                            "The number of invocations and total response time of this RESTful " +
                                    "resource method since the start of the server.")
                    .withUnit(MetricUnits.NANOSECONDS)
                    .build();
            registry.timer(successMetadata, metricID.getTagsAsArray());
        }
        MetricID metricIDForFailure = transformToMetricIDForFailedRequest(metricID);
        if (registry.getCounter(metricIDForFailure) == null) {
            Metadata failureMetadata = Metadata.builder()
                    .withName(metricIDForFailure.getName())
                    .withDescription(
                            "The total number of unmapped exceptions that occurred from this RESTful resource " +
                                    "method since the start of the server.")
                    .build();
            registry.counter(failureMetadata, metricIDForFailure.getTagsAsArray());
        }
    }

}
