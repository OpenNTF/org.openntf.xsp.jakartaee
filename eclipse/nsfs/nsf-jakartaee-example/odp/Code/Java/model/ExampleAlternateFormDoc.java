package model;

import java.util.stream.Stream;

import org.openntf.xsp.jakarta.nosql.mapping.extension.DominoRepository;
import org.openntf.xsp.jakarta.nosql.mapping.extension.FormName;
import org.openntf.xsp.jakarta.nosql.mapping.extension.ViewEntries;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

@Entity
@FormName("AlternateForm")
public class ExampleAlternateFormDoc {
	public interface Repository extends DominoRepository<ExampleAlternateFormDoc, String> {
		@ViewEntries("AlternateForm Documents")
		Stream<ExampleAlternateFormDoc> listAll();
	}

	@Id
	private String unid;
	@Column
	private String name;
	
	public String getUnid() {
		return unid;
	}
	public void setUnid(String unid) {
		this.unid = unid;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
