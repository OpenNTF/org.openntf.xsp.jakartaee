package model;

import jakarta.data.repository.CrudRepository;
import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

/**
 * This entity is meant to test the ability to use stock Jakarta Data
 * repository types in a Domino context
 */
@Entity
public record BareRepoDoc(
	@Id String unid,
	@Column String title
	) {
	public interface Repository extends CrudRepository<BareRepoDoc, String> {
		
	}
}
