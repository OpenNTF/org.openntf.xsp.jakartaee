package bean;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
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
	public void postConstruct() { System.out.println("Created!"); }
	@PreDestroy
	public void preDestroy() { System.out.println("Destroying!");  }
}
