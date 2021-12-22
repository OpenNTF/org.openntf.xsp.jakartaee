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
