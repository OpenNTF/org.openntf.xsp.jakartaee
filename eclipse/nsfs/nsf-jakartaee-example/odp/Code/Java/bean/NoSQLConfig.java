package bean;

import org.openntf.xsp.jakarta.nosql.driver.ExplainEvent;
import org.openntf.xsp.jakarta.nosql.driver.NoSQLConfigurationBean;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.event.Observes;

@RequestScoped
public class NoSQLConfig implements NoSQLConfigurationBean {
	
	private boolean explainEvents;
	private ExplainEvent lastEvent;
	
	public void setExplainEvents(boolean explainEvents) {
		this.explainEvents = explainEvents;
	}
	
	public ExplainEvent getLastEvent() {
		return lastEvent;
	}
	
	public void listenLastEvent(@Observes ExplainEvent event) {
		this.lastEvent = event;
	}

	@Override
	public boolean emitExplainEvents() {
		return explainEvents;
	}

}
