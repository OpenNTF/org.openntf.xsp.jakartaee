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
package org.openntf.xsp.jakartaee.bridge.jasapi.module;

import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import javax.servlet.ServletException;

import com.ibm.commons.util.StringUtil;
import com.ibm.designer.runtime.domino.adapter.LCDEnvironment;
import com.ibm.domino.bridge.http.jasapi.IJavaSapiEnvironment;
import com.ibm.domino.bridge.http.jasapi.IJavaSapiHttpContextAdapter;
import com.ibm.domino.bridge.http.jasapi.IJavaSapiHttpRequestAdapter;
import com.ibm.domino.bridge.http.jasapi.JavaSapiService;
import com.ibm.domino.xsp.module.nsf.NSFComponentModule;
import com.ibm.domino.xsp.module.nsf.NSFService;
import com.ibm.domino.xsp.module.nsf.NotesContext;

import org.openntf.xsp.jakartaee.jasapi.JavaSapiExtension;
import org.openntf.xsp.jakartaee.util.PriorityComparator;

/**
 * Provides a JavaSapi bridge for NSFs.
 *
 * @author Jesse Gallagher
 * @since 2.13.0
 */
public class NSFComponentModuleJavaSapiService extends JavaSapiService {
	private NSFService nsfService;

	public NSFComponentModuleJavaSapiService(final IJavaSapiEnvironment env) {
		super(env);
	}

	@Override
	public int authenticate(final IJavaSapiHttpContextAdapter context) {
		try {
			// TODO figure out the "reported the following problem causing authentication to fail: File does not exist" thing
			return withExtensions(context, ext -> ext.authenticate(new DelegatingJavaSapiContext(context))).getStatus();
		} catch(Throwable t) {
			t.printStackTrace();
		}
		return HTEXTENSION_EVENT_DECLINED;
	}

	@Override
	public void startRequest(final IJavaSapiHttpContextAdapter context) {
		// Too early to call into the NSF
	}

	@Override
	public void endRequest(final IJavaSapiHttpContextAdapter context) {
		try {
			withExtensions(context, ext -> {
				ext.authenticate(new DelegatingJavaSapiContext(context));
				return JavaSapiExtension.Result.EVENT_DECLINED;
			});
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}

	@Override
	public String getServiceName() {
		return getClass().getSimpleName();
	}

	@Override
	public int processRequest(final IJavaSapiHttpContextAdapter context) {
		try {
			return withExtensions(context, ext -> ext.processRequest(new DelegatingJavaSapiContext(context))).getStatus();
		} catch(Throwable t) {
			t.printStackTrace();
		}
		return HTEXTENSION_EVENT_DECLINED;
	}

	@Override
	public int rawRequest(final IJavaSapiHttpContextAdapter context) {
		try {
			return withExtensions(context, ext -> ext.rawRequest(new DelegatingJavaSapiContext(context))).getStatus();
		} catch(Throwable t) {
			t.printStackTrace();
		}
		return HTEXTENSION_EVENT_DECLINED;
	}

	@Override
	public int rewriteURL(final IJavaSapiHttpContextAdapter context) {
		try {
			return withExtensions(context, ext -> ext.rewriteURL(new DelegatingJavaSapiContext(context))).getStatus();
		} catch(Throwable t) {
			t.printStackTrace();
		}
		return HTEXTENSION_EVENT_DECLINED;
	}

	private JavaSapiExtension.Result withExtensions(final IJavaSapiHttpContextAdapter context, final Function<JavaSapiExtension, JavaSapiExtension.Result> c) throws ServletException {
		IJavaSapiHttpRequestAdapter req = context.getRequest();
		String path = StringUtil.toString(req.getRequestURI());
		int nsfIndex = path.toLowerCase().indexOf(".nsf"); //$NON-NLS-1$
		if(nsfIndex > -1 && path.length() > 4) {
			String moduleName = path.substring(1, nsfIndex+4);
			NSFComponentModule mod = getNsfService().loadModule(moduleName);
			if(mod != null) {
				// TODO cache cache cache
				// TODO invalidate that cache

				NotesContext.initThread(new NotesContext(mod));
				try {
					ServiceLoader<JavaSapiExtension> extensions = ServiceLoader.load(JavaSapiExtension.class, mod.getModuleClassLoader());
					return StreamSupport.stream(extensions.spliterator(), false)
						.sorted(PriorityComparator.DESCENDING)
						.map(c::apply)
						.filter(r -> r != null && r != JavaSapiExtension.Result.EVENT_DECLINED)
						.findFirst()
						.orElse(JavaSapiExtension.Result.EVENT_DECLINED);
				} finally {
					NotesContext.termThread();
				}
			}
		}
		return JavaSapiExtension.Result.EVENT_DECLINED;
	}

	private synchronized NSFService getNsfService() {
		if(this.nsfService == null) {
			LCDEnvironment lcd = LCDEnvironment.getInstance();
			this.nsfService = lcd.getServices().stream()
				.filter(NSFService.class::isInstance)
				.map(NSFService.class::cast)
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("Unable to locate active NSFService"));
		}
		return this.nsfService;
	}
}
