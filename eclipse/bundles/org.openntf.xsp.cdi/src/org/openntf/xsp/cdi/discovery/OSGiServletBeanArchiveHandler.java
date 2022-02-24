/**
 * Copyright © 2018-2022 Jesse Gallagher
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
package org.openntf.xsp.cdi.discovery;

import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.environment.deployment.discovery.BeanArchiveBuilder;
import org.jboss.weld.environment.deployment.discovery.BeanArchiveHandler;
import org.openntf.xsp.cdi.util.ContainerUtil;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.osgi.framework.Bundle;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.domino.napi.NotesDatabase;
import com.ibm.domino.osgi.core.context.ContextInfo;

import jakarta.annotation.Priority;

/**
 * 
 * @author Jesse Gallagher
 * @since 1.2.0
 */
@Priority(Integer.MAX_VALUE-1)
public class OSGiServletBeanArchiveHandler implements BeanArchiveHandler {
	public static final ThreadLocal<Bundle> PROCESSING_BUNDLE = new ThreadLocal<>();
	public static final ThreadLocal<String> PROCESSING_ID = new ThreadLocal<>();

	@Override
	public BeanArchiveBuilder handle(String beanArchiveReference) {
		try {
			Bundle bundle = PROCESSING_BUNDLE.get();
			if(bundle == null) {
				NotesDatabase database = ContextInfo.getServerDatabase();
				if(database != null) {
					String bundleName = ContainerUtil.getApplicationCDIBundle(database);
					if(StringUtil.isNotEmpty(bundleName)) {
						bundle = LibraryUtil.getBundle(bundleName).orElse(null);
					} else {
						bundleName = ContainerUtil.getApplicationCDIBundleBase(database);
						if(StringUtil.isNotEmpty(bundleName)) {
							bundle = LibraryUtil.getBundle(bundleName).orElse(null);
						}
					}
				}
			}
			
			if(bundle != null) {
				String symbolicName = bundle.getSymbolicName();
				// Slightly customize the builder to keep some extra metadata
				BeanArchiveBuilder builder = new BeanArchiveBuilder() {
					{
						super.setBeansXml(BeansXml.EMPTY_BEANS_XML);
						super.setId(symbolicName);
					}
					
					@Override
					public BeanArchiveBuilder setBeansXml(BeansXml beansXml) {
						return this;
					}
				};
				
				return builder;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
