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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SseChatBean {
	private Collection<BlockingQueue<String>> queues;
	
	@PostConstruct
	public void init() {
		queues = Collections.synchronizedSet(new HashSet<>());
	}
	
	public BlockingQueue<String> listen() {
		BlockingQueue<String> result = new LinkedBlockingDeque<>();
		queues.add(result);
		return result;
	}
	
	public void unregister(BlockingQueue<String> queue) {
		this.queues.remove(queue);
	}
	
	public void publish(String message) {
		queues.forEach(queue -> queue.offer(message));
	}
}
