package model;

import java.io.Serializable;
import java.util.List;

import org.openntf.xsp.nosql.communication.driver.DominoConstants;
import org.openntf.xsp.nosql.mapping.extension.DXLExport;
import org.openntf.xsp.nosql.mapping.extension.ItemFlags;
import org.openntf.xsp.nosql.mapping.extension.ItemStorage;

import jakarta.nosql.mapping.Column;
import jakarta.nosql.mapping.Entity;
import jakarta.nosql.mapping.Id;

@Entity
public class ExampleDoc {
	public static class JsonStorage {
		private String firstName;
		private String lastName;
		
		public String getFirstName() {
			return firstName;
		}
		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}
		public String getLastName() {
			return lastName;
		}
		public void setLastName(String lastName) {
			this.lastName = lastName;
		}
	}
	public static class MimeStorage implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private String title;
		private String address;
		
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public String getAddress() {
			return address;
		}
		public void setAddress(String address) {
			this.address = address;
		}
	}
	
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
	@ItemStorage(type=ItemStorage.Type.MIME)
	private String body;
	@Column("JSONGuy")
	@ItemStorage(type=ItemStorage.Type.JSON)
	private JsonStorage jsonGuy;
	@Column("MIMEGuy")
	@ItemStorage(type=ItemStorage.Type.MIMEBean)
	private MimeStorage mimeGuy;
	
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
	
	public JsonStorage getJsonGuy() {
		return jsonGuy;
	}
	public void setJsonGuy(JsonStorage jsonGuy) {
		this.jsonGuy = jsonGuy;
	}
	
	public MimeStorage getMimeGuy() {
		return mimeGuy;
	}
	public void setMimeGuy(MimeStorage mimeGuy) {
		this.mimeGuy = mimeGuy;
	}
}
