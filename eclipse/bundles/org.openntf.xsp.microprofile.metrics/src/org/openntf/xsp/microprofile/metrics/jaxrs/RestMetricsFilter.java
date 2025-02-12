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
package org.openntf.xsp.microprofile.metrics.jaxrs;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.Tag;
import org.openntf.xsp.jakartaee.metrics.MetricsIgnore;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.microprofile.metrics.config.MetricsAppConfigSource;

import io.smallrye.metrics.jaxrs.JaxRsMetricsFilter;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;

/**
 * This filter is based on {@link JaxRsMetricsFilter} but adds support for app-specific
 * tags.
 *
 * @author Jesse Gallagher
 * @author Red Hat, Inc. and/or its affiliates
 * @since 2.10.0
 */
public class RestMetricsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Context
    ResourceInfo resourceInfo;

    @Override
    public void filter(final ContainerRequestContext requestContext) {
    	Class<?> resourceClass = resourceInfo.getResourceClass();
    	if(resourceClass.isAnnotationPresent(MetricsIgnore.class)) {
    		return;
    	}

        MetricID metricID = getMetricID(resourceClass, resourceInfo.getResourceMethod());
        // store the MetricID so that the servlet filter can update the metric
        requestContext.setProperty("smallrye.metrics.jaxrs.metricID", metricID); //$NON-NLS-1$
    }

    private MetricID getMetricID(final Class<?> resourceClass, final Method resourceMethod) {
    	List<Tag> tags = new ArrayList<>();

    	// Class-level tag
        Tag classTag = new Tag("class", resourceClass.getName()); //$NON-NLS-1$
        String methodName = resourceMethod.getName();
        String encodedParameterNames = Arrays.stream(resourceMethod.getParameterTypes())
                .map(clazz -> {
                    if (clazz.isArray()) {
                        return clazz.getComponentType().getName() + "[]"; //$NON-NLS-1$
                    } else {
                        return clazz.getName();
                    }
                })
                .collect(Collectors.joining("_")); //$NON-NLS-1$
        tags.add(classTag);

        // Method-level tag
        String methodTagValue = encodedParameterNames.isEmpty() ? methodName : methodName + "_" + encodedParameterNames; //$NON-NLS-1$
        Tag methodTag = new Tag("method", methodTagValue); //$NON-NLS-1$
        tags.add(methodTag);

        // App-level tag when present
        if(LibraryUtil.isLibraryActive(LibraryUtil.LIBRARY_MICROPROFILE)) {
			Config mpConfig = CDI.current().select(Config.class).get();
			mpConfig.getOptionalValue(MetricsAppConfigSource.CONFIG_APPNAME, String.class)
				.map(appName -> new Tag(MetricsAppConfigSource.TAG_APP, appName))
				.ifPresent(tags::add);
        }

        return new MetricID("REST.request", tags.toArray(new Tag[tags.size()])); //$NON-NLS-1$
    }

    @Override
    public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) throws IOException {
        // If the response filter is called, it means the processing did NOT end with
        // an unmapped exception. Store this information for the servlet filter so
        // that it knows which metric to update.
        requestContext.setProperty("smallrye.metrics.jaxrs.successful", true); //$NON-NLS-1$
    }
}
