/**
 * Copyright © 2018-2021 Jesse Gallagher
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
package org.openntf.xsp.el;

import javax.faces.application.Application;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;

import com.ibm.xsp.binding.BindingFactory;
import com.sun.faces.el.MethodBindingImpl;
import com.sun.faces.el.ValueBindingImpl;
import com.sun.faces.util.Util;

public class XSPELBindingFactory implements BindingFactory {

	public static final String IBM_PREFIX = "xspel"; //$NON-NLS-1$
	
	private final String prefix;
	
	public XSPELBindingFactory(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public String getPrefix() {
		return prefix;
	}

	@Override
	public ValueBinding createValueBinding(Application application, String ref) {
		ValueBindingImpl result = new ValueBindingImpl(application);
		result.setRef(Util.stripBracketsIfNecessary(cleanRef(ref)));
		return result;
	}

	@Override
	public MethodBinding createMethodBinding(Application application, String ref, @SuppressWarnings("rawtypes") Class[] args) {
		return new MethodBindingImpl(application, cleanRef(ref), args);
	}

	private String cleanRef(String ref) {
		if(ref.startsWith("#{" + prefix + ':')) { //$NON-NLS-1$
			return ref.substring(0, 2) + ref.substring(prefix.length()+3);
		} else {
			return ref;
		}
	}
}
