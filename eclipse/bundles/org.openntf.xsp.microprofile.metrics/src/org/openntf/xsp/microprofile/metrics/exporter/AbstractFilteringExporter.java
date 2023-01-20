package org.openntf.xsp.microprofile.metrics.exporter;

import java.util.Map;

import org.eclipse.microprofile.metrics.MetricID;
import org.openntf.xsp.microprofile.metrics.config.MetricsAppConfigSource;

import io.smallrye.metrics.exporters.Exporter;

/**
 * 
 * @author Jesse Gallagher
 * @since 2.10.0
 */
public abstract class AbstractFilteringExporter implements Exporter {
	private final String appName;
	
	public AbstractFilteringExporter(String appName) {
		this.appName = appName;
	}
	
	protected boolean matchesApp(MetricID id) {
		if (appName == null || appName.isEmpty()) {
			return true;
		} else {
			Map<String, String> idTags = id.getTags();
			if (idTags != null) {
				String tagAppName = idTags.get(MetricsAppConfigSource.TAG_APP);
				if(tagAppName == null) {
					return true;
				} else {
					return appName.equals(idTags.get(MetricsAppConfigSource.TAG_APP));
				}
			} else {
				return true;
			}
		}
	}
}
