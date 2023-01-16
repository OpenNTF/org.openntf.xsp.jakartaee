/**
 * Copyright (c) 2018-2023 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.jakartaee.weaving;

import com.ibm.designer.runtime.domino.adapter.HttpService;
import com.ibm.designer.runtime.domino.adapter.IServiceFactory;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;

/**
 * This stub {@link IServiceFactory} implementation exists solely to
 * ensure that this bundle is activated early in HTTP initialization
 * to get {@link UtilWeavingHook} in operation.
 * 
 * @author Jesse Gallagher
 * @since 2.2.0
 */
public class WeavingEarlyInitFactory implements IServiceFactory {
	@Override
	public HttpService[] getServices(LCDEnvironment env) {
		return new HttpService[0];
	}
}
