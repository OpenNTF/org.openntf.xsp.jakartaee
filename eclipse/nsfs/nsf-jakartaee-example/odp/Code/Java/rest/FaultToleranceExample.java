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
package rest;

import bean.FaultToleranceBean;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("faultTolerance")
public class FaultToleranceExample {
	@Inject
	private FaultToleranceBean bean;
	
	@Path("retry")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getRetry() {
		return bean.getFailing();
	}
	
	@Path("timeout")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getTimeout() throws InterruptedException {
		return bean.getTimeout();
	}
	
	@Path("circuitBreaker")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String getCircuitBreaker() {
		return bean.getCircuitBreaker();
	}
}
