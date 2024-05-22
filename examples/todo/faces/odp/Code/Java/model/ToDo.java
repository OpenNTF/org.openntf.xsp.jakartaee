package model;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.stream.Stream;

import org.openntf.xsp.jakarta.nosql.mapping.extension.DominoRepository;
import org.openntf.xsp.jakarta.nosql.mapping.extension.RepositoryProvider;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;
import jakarta.data.Sort;
import jakarta.validation.constraints.NotEmpty;

@Entity("To-Do")
public class ToDo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@RepositoryProvider("current")
	public interface Repository extends DominoRepository<ToDo, String> {
		Stream<ToDo> findAll(Sort<?> sorts);
		
		Stream<ToDo> findByStatus(State status, Sort<?> sorts);
	}
	
	public enum State {
		Incomplete, Complete
	}
	
	@Id
	private String documentId;
	@Column("Title") @NotEmpty
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
