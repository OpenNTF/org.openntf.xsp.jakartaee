/**
 * Copyright © 2018 Jesse Gallagher
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

import javax.annotation.Priority;

import org.jboss.weld.bootstrap.spi.BeansXml;
import org.jboss.weld.environment.deployment.discovery.BeanArchiveBuilder;
import org.jboss.weld.environment.deployment.discovery.BeanArchiveHandler;
import org.openntf.xsp.jakartaee.ModuleUtil;

import com.ibm.domino.xsp.module.nsf.NSFComponentModule;
import com.ibm.domino.xsp.module.nsf.NotesContext;

@SuppressWarnings("restriction")
@Priority(Integer.MAX_VALUE)
public class NSFBeanArchiveHandler implements BeanArchiveHandler {

	public NSFBeanArchiveHandler() {
	}

	@Override
	public BeanArchiveBuilder handle(String beanArchiveReference) {
		NotesContext context = NotesContext.getCurrentUnchecked();
		if(context != null) {
			NSFComponentModule module = context.getModule();
			// Slightly customize the builder to keep some extra metadata
			BeanArchiveBuilder builder = new BeanArchiveBuilder() {
				{
					super.setBeansXml(BeansXml.EMPTY_BEANS_XML);
					super.setId(module.getDatabasePath());
				}
				
				@Override
				public BeanArchiveBuilder setBeansXml(BeansXml beansXml) {
					return this;
				}
			};
			
			ModuleUtil.getClassNames(module)
				.filter(className -> !ModuleUtil.GENERATED_CLASSNAMES.matcher(className).matches())
				.forEach(builder::addClass);
			
			
			// Manually look for class names in plug-in dependencies, since the normal code
			//  path only looks in the system class path and I haven't figured out the right
			//  way to override that yet
			
			
			return builder;
		} else {
			return null;
		}
	}

}
