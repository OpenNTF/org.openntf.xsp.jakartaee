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
package bean;

import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FaultToleranceBean {
	@Retry(maxRetries = 2)
	@Fallback(fallbackMethod = "getFailingFallback")
	public String getFailing() {
		throw new RuntimeException("this is expected to fail");
	}
	
	@SuppressWarnings("unused")
	private String getFailingFallback() {
		return "I am the fallback response.";
	}
	
	@Timeout(value=5, unit=ChronoUnit.MILLIS)
	public String getTimeout() throws InterruptedException {
		TimeUnit.MILLISECONDS.sleep(10);
		return "I should have stopped.";
	}
	
	@CircuitBreaker(delay=60000, requestVolumeThreshold=2)
	public String getCircuitBreaker() {
		throw new RuntimeException("I am a circuit-breaking failure - I should stop after two attempts");
	}
}
