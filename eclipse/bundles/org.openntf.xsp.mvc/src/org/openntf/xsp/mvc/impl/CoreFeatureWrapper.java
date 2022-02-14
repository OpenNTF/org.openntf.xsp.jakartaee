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
package org.openntf.xsp.mvc.impl;

import org.eclipse.krazo.bootstrap.CoreFeature;
import org.openntf.xsp.jakartaee.util.LibraryUtil;
import org.openntf.xsp.mvc.MvcLibrary;

import jakarta.ws.rs.ConstrainedTo;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.Provider;

@Provider
@ConstrainedTo(RuntimeType.SERVER)
public class CoreFeatureWrapper extends CoreFeature {

	@Override
	public boolean configure(FeatureContext context) {
		if(LibraryUtil.isLibraryActive(MvcLibrary.LIBRARY_ID)) {
			return super.configure(context);
		}
		return false;
	}

}
