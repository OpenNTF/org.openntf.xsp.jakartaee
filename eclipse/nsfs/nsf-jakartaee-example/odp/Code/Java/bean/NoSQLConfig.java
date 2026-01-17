/**
 * Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
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
package bean;

import org.openntf.xsp.jakarta.nosql.driver.ExplainEvent;
import org.openntf.xsp.jakarta.nosql.driver.NoSQLConfigurationBean;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Observes;

@RequestScoped
public class NoSQLConfig implements NoSQLConfigurationBean {
	
	private boolean explainEvents;
	private ExplainEvent lastEvent;
	
	public void setExplainEvents(boolean explainEvents) {
		this.explainEvents = explainEvents;
	}
	
	public ExplainEvent getLastEvent() {
		return lastEvent;
	}
	
	public void listenLastEvent(@Observes ExplainEvent event) {
		this.lastEvent = event;
	}

	@Override
	public boolean emitExplainEvents() {
		return explainEvents;
	}

}
