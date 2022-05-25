/**
 * Copyright © 2018-2022 Contributors to the XPages Jakarta EE Support Project
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
package org.openntf.xsp.test.beanbundle.servlet;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.openntf.xsp.test.beanbundle.servlet.resources.BundleBeanResource;
import org.openntf.xsp.test.beanbundle.servlet.resources.RootResource;

import jakarta.ws.rs.core.Application;

public class ExampleApplication extends Application {
	@Override
	public Set<Class<?>> getClasses() {
		return new HashSet<>(Arrays.asList(
			RootResource.class,
			BundleBeanResource.class
		));
	}
}
