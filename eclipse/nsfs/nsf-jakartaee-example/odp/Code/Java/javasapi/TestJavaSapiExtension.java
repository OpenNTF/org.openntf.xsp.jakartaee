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
package javasapi;

import org.openntf.xsp.jakartaee.jasapi.JavaSapiContext;
import org.openntf.xsp.jakartaee.jasapi.JavaSapiExtension;

import com.ibm.commons.util.StringUtil;

public class TestJavaSapiExtension implements JavaSapiExtension {

	@Override
	public Result rawRequest(JavaSapiContext context) {
		context.getResponse().setHeader("X-InNSFCustomHeader", "Hello from NSF"); //$NON-NLS-1$ //$NON-NLS-2$
		return Result.SUCCESS;
	}
	
	@Override
	public Result authenticate(JavaSapiContext context) {
		String overrideName = context.getRequest().getHeader("X-OverrideName");
		if(StringUtil.isNotEmpty(overrideName)) {
			context.getRequest().setAuthenticatedUserName(overrideName, "Basic");
			return Result.REQUEST_AUTHENTICATED;
		}
		return JavaSapiExtension.super.authenticate(context);
	}

}
