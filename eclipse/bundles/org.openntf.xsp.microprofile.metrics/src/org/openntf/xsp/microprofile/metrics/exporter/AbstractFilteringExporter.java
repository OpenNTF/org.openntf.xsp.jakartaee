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
