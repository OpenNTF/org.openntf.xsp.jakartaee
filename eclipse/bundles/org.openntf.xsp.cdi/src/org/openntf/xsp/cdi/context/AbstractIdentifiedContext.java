/**
 * Copyright © 2020 Jesse Gallagher
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
package org.openntf.xsp.cdi.context;

import java.lang.annotation.Annotation;

import org.jboss.weld.contexts.AbstractSharedContext;

public class AbstractIdentifiedContext extends AbstractSharedContext {
	private final String id;
	private final Class<? extends Annotation> scope;
	private boolean active = true;

	public AbstractIdentifiedContext(String contextId, String id, Class<? extends Annotation> scope) {
		super(contextId);
		this.id = id;
		this.scope = scope;
	}

	@Override
	public Class<? extends Annotation> getScope() {
		return scope;
	}
	
	public String getId() {
		return id;
	}
	
	@Override
	public void invalidate() {
		super.invalidate();
		this.active = false;
	}
	
	@Override
	public boolean isActive() {
		return active;
	}

}
