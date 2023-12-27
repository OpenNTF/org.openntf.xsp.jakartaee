package model;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.stream.Stream;

import org.openntf.xsp.nosql.mapping.extension.DominoRepository;

import jakarta.nosql.mapping.Column;
import jakarta.nosql.mapping.Entity;
import jakarta.nosql.mapping.Id;
import jakarta.nosql.mapping.Sorts;

@Entity("To-Do")
public class ToDo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public interface Repository extends DominoRepository<ToDo, String> {
		Stream<ToDo> findAll(Sorts sorts);
		
		Stream<ToDo> findByStatus(State status, Sorts sorts);
	}
	
	public enum State {
		Incomplete, Complete
	}
	
	@Id
	private String documentId;
	@Column("Title")
	private String title;
	@Column("Created")
	private OffsetDateTime created;
	@Column("Status")
	private State status;
	
	public String getDocumentId() {
		return documentId;
	}
	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	public OffsetDateTime getCreated() {
		return created;
	}
	public void setCreated(OffsetDateTime created) {
		this.created = created;
	}
	
	// XPages doesn't understand OffsetDateTime, so add legacy access
	public Date getCreatedDate() {
		if(this.created == null) {
			return null;
		} else {
			return Date.from(this.created.toInstant());
		}
	}
	public void setCreatedDate(Date created) {
		if(created == null) {
			this.created = null;
		} else {
			this.created = OffsetDateTime.ofInstant(created.toInstant(), ZoneId.systemDefault());
		}
	}
	
	public State getStatus() {
		return status;
	}
	public void setStatus(State status) {
		this.status = status;
	}
	
	@Override
	public String toString() {
		return "ToDo [documentId=" + documentId + ", title=" + title + ", created=" + created + ", status=" + status
				+ "]";
	}
}
