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
package bean;

import java.io.Serializable;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@SessionScoped
@Named("sessionGuy")
public class SessionGuy implements Serializable {
	private static final long serialVersionUID = 1L;

	
	@Inject
	private ApplicationGuy applicationGuy;
	private final long time = System.currentTimeMillis();

	public String getMessage() {
		return "I'm session guy at " + time + ", using applicationGuy: " + applicationGuy.getMessage();
	}
	
	@PostConstruct
	public void postConstruct() { System.out.println("Created sessionGuy!"); }
	@PreDestroy
	public void preDestroy() { System.out.println("Destroying sessionGuy!");  }
}
