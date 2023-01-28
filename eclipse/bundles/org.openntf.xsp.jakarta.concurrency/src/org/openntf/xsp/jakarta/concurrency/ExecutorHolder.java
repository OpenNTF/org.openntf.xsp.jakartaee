package org.openntf.xsp.jakarta.concurrency;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class to house references to all active executors, so they can
 * be mass closed during task shutdown.
 * 
 * @author Jesse Gallagher
 * @since 2.10.0
 */
public enum ExecutorHolder {
	INSTANCE;
	
	private static final Logger log = Logger.getLogger(ExecutorHolder.class.getPackage().getName());
	
	private final Collection<ExecutorService> executors = Collections.synchronizedSet(new HashSet<>());
	
	public void register(ExecutorService exec) {
		this.executors.add(exec);
	}
	
	public void unregister(ExecutorService exec) {
		this.executors.remove(exec);
	}
	
	public synchronized void termAll() {
		executors.forEach(exec -> {
			if(!(exec.isShutdown() || exec.isTerminated())) {
				try {
					exec.shutdownNow();
					exec.awaitTermination(5, TimeUnit.MINUTES);
				} catch (Exception e) {
					if(log.isLoggable(Level.SEVERE)) {
						log.log(Level.SEVERE, "Encountered exception terminating scheduled executor service", e);
					}
				}
			}
		});
		executors.clear();
	}
}
