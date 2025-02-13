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

import java.util.concurrent.CompletableFuture;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.concurrent.Asynchronous;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@RequestScoped
@Named("requestGuy")
public class RequestGuy {
	@Inject
	private ApplicationGuy applicationGuy;
	private final long time = System.currentTimeMillis();

	public String getMessage() {
		return "I'm request guy at " + time + ", using applicationGuy: " + applicationGuy.getMessage();
	}
	
	@PostConstruct
	public void postConstruct() { System.out.println("Created requestGuy!"); }
	@PreDestroy
	public void preDestroy() { System.out.println("Destroying requestGuy!");  }
	
	@Asynchronous
	public CompletableFuture<String> getAsyncMessage() {
		long id = Thread.currentThread().getId();
		return CompletableFuture.completedFuture("I was run on thread " + id);
	}
}
