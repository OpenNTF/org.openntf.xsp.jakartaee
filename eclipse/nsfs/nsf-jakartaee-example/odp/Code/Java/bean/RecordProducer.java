package bean;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import model.RecordExample;

/**
 * This bean is used by tests to ensure that reading record properties in
 * XPages, JSP, and Faces contexts works.
 */
@RequestScoped
@Named("RecordProducer")
public class RecordProducer {
	public RecordExample getRecordExample() {
		return new RecordExample("1111", "I am the example", 3);
	}
}
