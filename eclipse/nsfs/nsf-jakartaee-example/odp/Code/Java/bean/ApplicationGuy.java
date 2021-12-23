package bean;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@ApplicationScoped
@Named("applicationGuy")
@XmlRootElement(name="application-guy")
public class ApplicationGuy {
	@XmlElement(name="time")
	private final long time = System.currentTimeMillis();
	@XmlElement(name="postConstructSet")
	private String postConstructSet;
	
	@JsonbProperty(value="jsonMessage")
	public String getMessage() {
		return "I'm application guy at " + time;
	}
	
	public String getMessageWithArg(String arg) {
		return "I've been told " + arg;
	}
	@PostConstruct
	public void postConstruct() {
		System.out.println("Created applicationGuy!");
	}
	@PreDestroy
	public void preDestroy() { System.out.println("Destroying applicationGuy!");  }
}
