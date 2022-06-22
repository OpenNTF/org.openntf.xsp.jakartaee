package model;

import java.util.List;

import org.openntf.xsp.nosql.communication.driver.DominoConstants;
import org.openntf.xsp.nosql.mapping.extension.DXLExport;
import org.openntf.xsp.nosql.mapping.extension.ItemFlags;

import jakarta.nosql.mapping.Column;
import jakarta.nosql.mapping.Entity;
import jakarta.nosql.mapping.Id;

@Entity
public class ExampleDoc {
	@Id
	private String unid;
	@Column("$$Title")
	private String title;
	@Column("$$Categories")
	private List<String> categories;
	@Column("Authors")
	@ItemFlags(authors=true)
	private List<String> authors;
	@Column("Body")
	private String body;
	
	@Column(DominoConstants.FIELD_DXL)
	@DXLExport(forceNoteFormat=true, encapsulateRichText=false, outputDOCTYPE=false)
	private String dxl;

	public String getUnid() {
		return unid;
	}
	public void setUnid(String unid) {
		this.unid = unid;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	public List<String> getCategories() {
		return categories;
	}
	public void setCategories(List<String> categories) {
		this.categories = categories;
	}

	public List<String> getAuthors() {
		return authors;
	}
	public void setAuthors(List<String> authors) {
		this.authors = authors;
	}
	
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}

	public String getDxl() {
		return dxl;
	}
}
