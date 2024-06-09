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
package org.openntf.xsp.jakartaee.bridge.jasapi;

import java.util.Collection;

import com.ibm.domino.bridge.http.jasapi.IJavaSapiEnvironment;
import com.ibm.domino.bridge.http.jasapi.JavaSapiService;

/**
 * This extention point interface allows for plugins to register JavaSapi
 * services that will be loaded at HTTP init.
 * 
 * @author Jesse Gallagher
 * @since 2.12.0
 */
public interface JasapiServiceFactory {
	public static final String EXTENSION_ID = JasapiServiceFactory.class.getName();

	Collection<JavaSapiService> getServices(IJavaSapiEnvironment env);

}
