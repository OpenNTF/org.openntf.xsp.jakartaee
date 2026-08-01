package bean;

import java.util.List;

import javax.faces.model.SelectItem;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.json.bind.Jsonb;

/**
 * Used to test https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/767
 */
@ApplicationScoped
@Named
public class SelectOptionsBean {
	@Inject
	private Jsonb jsonb;
	
	public List<SelectItem> getOptions() {
		return List.of(new SelectItem("foo"), new SelectItem("bar"));
	}
	
	public String toJson(Object obj) {
		return jsonb.toJson(obj);
	}
}
