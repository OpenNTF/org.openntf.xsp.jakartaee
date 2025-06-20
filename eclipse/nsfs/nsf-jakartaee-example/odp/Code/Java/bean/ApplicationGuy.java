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
package bean;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.Startup;
import jakarta.inject.Named;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@ApplicationScoped
@Named("applicationGuy")
@XmlRootElement(name="application-guy")
@SuppressWarnings("nls")
public class ApplicationGuy {
	@XmlElement(name="time")
	private final long time = System.currentTimeMillis();
	@XmlElement(name="postConstructSet")
	private String postConstructSet;
	@XmlElement(name="startupSet")
	private String startupSet;
	private String beanProperty;
	
	@PostConstruct
	public void postConstruct() {
		System.out.println("Created applicationGuy!");
		this.postConstructSet = "I was set by postConstruct";
	}
	@PreDestroy
	public void preDestroy() { System.out.println("Destroying applicationGuy!");  }
	
	public void startup(@Observes Startup startup) {
		this.startupSet = "I was set by startup";
	}
	
	public String getBeanProperty() {
		return beanProperty;
	}
	
	public void setBeanProperty(String beanProperty) {
		this.beanProperty = beanProperty;
	}
	
	@JsonbProperty(value="jsonMessage")
	public String getMessage() {
		return "I'm application guy at " + time;
	}
	
	public String getMessageWithArg(String arg) {
		return "I've been told " + arg;
	}
}
