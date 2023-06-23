package model;

import org.openntf.xsp.nosql.communication.driver.DominoConstants;
import org.openntf.xsp.nosql.mapping.extension.DominoRepository;

import jakarta.nosql.mapping.Column;
import jakarta.nosql.mapping.Entity;
import jakarta.nosql.mapping.Id;

@Entity("ExampleProfile")
public class ProfileDoc {
	public interface Repository extends DominoRepository<ProfileDoc, String> {
		
	}
	
	@Id
	private String documentId;
	
	@Column(DominoConstants.FIELD_PROFILENAME)
	private String profileName;
	
	@Column(DominoConstants.FIELD_PROFILEKEY)
	private String noteUserName;
	
	@Column
	private String subject;
	
	@Column
	private String body;

	public String getDocumentId() {
		return documentId;
	}

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	public String getprofileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	public String getNoteUserName() {
		return noteUserName;
	}

	public void setNoteUserName(String noteUserName) {
		this.noteUserName = noteUserName;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	
}
