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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.enterprise.concurrent.Asynchronous;
import jakarta.enterprise.concurrent.Schedule;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@ApplicationScoped
@Named
public class ConcurrencyBean {
	private int scheduleRan = 0;
	
	public String getMessage() throws NamingException, InterruptedException, ExecutionException {
		ExecutorService exec = InitialContext.doLookup("java:comp/DefaultManagedExecutorService");
		return exec.submit(() -> "Hello from executor").get();
	}
	
	@Asynchronous(runAt = @Schedule(cron="* * * * * *"))
	public void runScheduled() {
		if(++scheduleRan == 5) {
			Asynchronous.Result.complete(null);
		}
	}
	
	public int getScheduleRan() {
		return scheduleRan;
	}
}
