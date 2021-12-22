package bean;

import java.io.Serializable;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@ConversationScoped
@Named
public class ViewGuy implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Inject
	private SessionGuy sessionGuy;
	private final long time = System.currentTimeMillis();

	public String getMessage() {
		return "I'm view guy at " + time + ", using sessionGuy: " + sessionGuy.getMessage();
	}
	
	@PostConstruct
	public void postConstruct() { System.out.println("Created!"); }
	@PreDestroy
	public void preDestroy() { System.out.println("Destroying!");  }
}