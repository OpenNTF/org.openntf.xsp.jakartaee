package model;

import org.openntf.xsp.nosql.communication.driver.DominoDocumentCollectionManager;
import org.openntf.xsp.nosql.communication.driver.lsxbe.impl.DefaultDominoDocumentCollectionManager;

import com.ibm.domino.xsp.module.nsf.NotesContext;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.nosql.mapping.Database;
import jakarta.nosql.mapping.DatabaseType;

/**
 * Workaround for present lack of sessionAsSigner in JSF for
 * QRP databases. This assumes that the current user has high powers
 * on the server.
 */
@RequestScoped
public class CurrentRepositoryProvider {
	@Produces
	@Database(value = DatabaseType.DOCUMENT, provider = "current")
	public DominoDocumentCollectionManager getCurrentManager() {
		return new DefaultDominoDocumentCollectionManager(
			() -> NotesContext.getCurrent().getCurrentDatabase(),
			() -> NotesContext.getCurrent().getCurrentSession()
		);
	}
}
