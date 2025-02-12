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
package org.openntf.xsp.microprofile.metrics.exporter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Meter.Type;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.exporter.common.TextFormat;

public class FilteringPrometheusMetricsExporer extends AbstractFilteringExporter {

    private static final String CLASS_NAME = FilteringPrometheusMetricsExporer.class.getName();
    private static final Logger LOGGER = Logger.getLogger(CLASS_NAME);

    private static final String NEW_LINE = "\n";
    private static final String PROM_HELP = "# HELP";
    private static final String PROM_TYPE = "# TYPE";

    private final List<MeterRegistry> prometheusRegistryList;

    private final PrometheusMeterRegistry prometheusMeterRegistry;

    public FilteringPrometheusMetricsExporer(final String appName) {
    	super(appName);

        final String METHOD_NAME = "PrometheusMetricsExporter";

        prometheusRegistryList = Metrics.globalRegistry.getRegistries().stream()
                .filter(PrometheusMeterRegistry.class::isInstance).collect(Collectors.toList());

        if (prometheusRegistryList == null || prometheusRegistryList.size() == 0) {
            throw new IllegalStateException("Prometheus registry was not found in the global registry");
        } else if (prometheusRegistryList.size() > 1) {
            /*
             * This shouldn't happen at all. The only Prometheus Meter Registry that can be created is by us.
             * Unless vendor allows access to the Micrometer API and the customer creates a Prometheus Meter
             * Registry.
             */
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.logp(Level.FINER, CLASS_NAME, METHOD_NAME,
                        "Found {0} instances of Prometheus MeterRegistry in the global registry, the first instance found will be used",
                        prometheusRegistryList.size());
            }
        }

        prometheusMeterRegistry = (PrometheusMeterRegistry) prometheusRegistryList.get(0);

    }

    @Override
    public String exportAllScopes() {
        StringBuilder sb = new StringBuilder();
        for (MeterRegistry meterRegistry : prometheusRegistryList) {
            PrometheusMeterRegistry promMeterRegistry = (PrometheusMeterRegistry) meterRegistry;
            // strip "# EOF"
            String scraped = promMeterRegistry.scrape(TextFormat.CONTENT_TYPE_004).replaceFirst("# EOF\r?\n?", "");
            sb.append(scraped);
        }
        return sb.toString();
    }

    @Override
    public String exportOneScope(final String scope) {
        String scrapeOutput = prometheusMeterRegistry.scrape(TextFormat.CONTENT_TYPE_004).replaceFirst("# EOF\r?\n?", "");
        return filterScope(scrapeOutput, scope);
    }

    @Override
    public String exportMetricsByName(final String scope, final String metricName) {
        return filterMetrics(metricName, scope);
    }

    @Override
    public String exportOneMetricAcrossScopes(final String metricName) {
        return filterMetrics(metricName);
    }

    public String filterMetrics(final String name) {
        return filterMetrics(name, null);
    }

    public String filterMetrics(final String metricName, final String scope) {

        Set<String> unitTypesSet = new HashSet<>();
        unitTypesSet.add("");
        Set<String> meterSuffixSet = new HashSet<>();
        meterSuffixSet.add("");

        for (Meter m : prometheusMeterRegistry.find(metricName).meters()) {
            unitTypesSet.add("_" + m.getId().getBaseUnit());
            resolveMeterSuffixes(meterSuffixSet, m.getId().getType());
        }

        Set<String> scrapeMeterNames = calculateMeterNamesToScrape(metricName, meterSuffixSet, unitTypesSet);
        // Strip #EOF from output
        String scrapeOutput = prometheusMeterRegistry.scrape(TextFormat.CONTENT_TYPE_004, scrapeMeterNames)
                .replaceFirst("\r?\n?# EOF", "");

        return scope == null || scope.isEmpty() ? scrapeOutput : filterScope(scrapeOutput, scope);
    }

    public String filterScope(final String scrapeOutput, final String scope) {
        String[] lines = scrapeOutput.split("\r?\n");

        StringBuilder outputBuilder = new StringBuilder();
        StringBuilder tempBuilder = new StringBuilder();
        String scopeString = "scope=\"" + scope + "\"";

        int metricLineCount = 0;
        for (String line : lines) {
            if (line.startsWith(PROM_HELP)) {
                /*
                 * Expect TYPE and HELP lines minimum (If using Prometheus client) Only add to the output
                 * if there exists actual metrics (i.e. line count > 2 )
                 */
                if (metricLineCount > 2) {
                    outputBuilder.append(tempBuilder);
                }
                metricLineCount = 0;
                tempBuilder = new StringBuilder();
            } else if (line.startsWith(PROM_TYPE)) {
                /*
                 * Do nothing - don't want this to be caught in the else if which checks if scope is not present and
                 * skips the for loop since we append this line at the very end
                 */
            } else if (!line.contains(scopeString)) {
                continue;
            }
            tempBuilder.append(line);
            tempBuilder.append(NEW_LINE);
            metricLineCount++;
        }

        if (metricLineCount > 2) {
            outputBuilder.append(tempBuilder);
        }

        return outputBuilder.toString();
    }

    /**
     * Since this implementation uses Micrometer, it prefixes any metric name that does not start with a
     * letter with "m_". Since we need to be able to query by a specific metric name we need to "format"
     * the metric name and to be similar to the output formatted by the Prometheus client. This will be
     * used with the pmr.scrape(String contenttype, Set<String> names) to get the metric we want.
     *
     * @param input
     * @return
     */
    private String resolvePrometheusName(final String input) {
        String output = input;

        // Change other special characters to underscore - Use to convert double underscores to single
        // underscore
        output = output.replaceAll("[-+.!?@#$%^&*`'\\s]+", "_");

        // Match with Prometheus simple client formatter where it appends "m_" if it starts with a number
        if (output.matches("^[0-9]+.*")) {
            output = "m_" + output;
        }

        // Match with Prometheus simple client formatter where it appends "m_" if it starts with a
        // "underscore"
        if (output.matches("^_+.*")) {
            output = "m_" + output;
        }

        // non-ascii characters to "" -- does this affect other languages?
        output = output.replaceAll("[^A-Za-z0-9_]", "");

        return output;
    }

    private void resolveMeterSuffixes(final Set<String> set, final Type inputType) {

        final String METHOD_NAME = "resolveMeterSuffixes";
        switch (inputType) {
            case COUNTER:
                set.add("_total");
                break;
            case DISTRIBUTION_SUMMARY:
            case TIMER:
            case LONG_TASK_TIMER:
                set.add("_count");
                set.add("_sum");
                set.add("_max");
                set.add("_bucket");
                break;
            default:
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.logp(Level.FINEST, CLASS_NAME, METHOD_NAME, "Unsupported Meter type: {0} ", inputType.name());
                }
                break;
        }
    }

    private Set<String> calculateMeterNamesToScrape(final String name, final Set<String> meterSuffixSet, final Set<String> unitTypeSet) {
        final String METHOD_NAME = "calculateMeterNamesToScrape";
        String promName = resolvePrometheusName(name);
        Set<String> retSet = new HashSet<>();
        for (String unit : unitTypeSet) {
            for (String suffix : meterSuffixSet) {
                retSet.add(promName + unit + suffix);
            }
        }
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.logp(Level.FINEST, CLASS_NAME, METHOD_NAME, "The meter names to scrape: " + retSet);
        }

        return retSet;
    }

    @Override
    public String getContentType() {
        return "text/plain";
    }

}
