/**
 * Copyright © 2019 Jesse Gallagher
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
package org.openntf.xsp.cdi.provider;

import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.CDIProvider;

import org.openntf.xsp.cdi.util.ContainerUtil;

import com.ibm.xsp.application.ApplicationEx;

/**
 * Provides access to the current application's Weld context.
 * 
 * @author Jesse Gallagher
 * @since 1.0.0
 */
public class NSFCDIProvider implements CDIProvider {

	@Override
	public CDI<Object> getCDI() {
		ApplicationEx application = ApplicationEx.getInstance();
		if(application != null) {
			return ContainerUtil.getContainer(application);
		}
		
		return null;
	}
	
	@Override
	public int getPriority() {
		return DEFAULT_CDI_PROVIDER_PRIORITY+2;
	}

}
