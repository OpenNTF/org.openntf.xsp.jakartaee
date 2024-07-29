package model;

import java.util.Optional;

import org.openntf.xsp.jakarta.nosql.mapping.extension.DominoRepository;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

@Entity("RecordExample")
public record RecordExample(
		@Id
		String unid,
		@Column
		String name,
		@Column
		int index
	) {
	
	public interface Repository extends DominoRepository<RecordExample, String> {
		Optional<RecordExample> findByName(String name);
	}
}
