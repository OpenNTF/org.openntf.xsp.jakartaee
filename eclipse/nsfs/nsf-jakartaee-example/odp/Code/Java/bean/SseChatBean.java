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
