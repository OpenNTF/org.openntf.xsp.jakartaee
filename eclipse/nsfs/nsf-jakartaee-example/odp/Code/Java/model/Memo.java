package model;

import java.util.List;
import java.util.stream.Stream;

import org.openntf.xsp.jakarta.nosql.mapping.extension.DominoRepository;
import org.openntf.xsp.jakarta.nosql.mapping.extension.RepositoryProvider;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

@Entity
public record Memo(
	@Id String documentId,
	@Column String subject,
	@Column String body,
	@Column List<String> sendTo
) {
	@RepositoryProvider("devMail")
	public interface MailfileRepository extends DominoRepository<Memo, String> {
		Stream<Memo> findBySubject(String subject);
	}
	
	public interface Repository extends DominoRepository<Memo, String> {
		Stream<Memo> findBySubject(String subject);
	}
}
