package model;

import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;
import org.openntf.xsp.jakarta.nosql.communication.driver.DominoDocumentManager;
import org.openntf.xsp.jakarta.nosql.communication.driver.lsxbe.impl.DefaultDominoDocumentCollectionManager;

import com.ibm.domino.xsp.module.nsf.NotesContext;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;

/**
 * Workaround for present lack of sessionAsSigner in JSF for
 * QRP databases. This assumes that the current user has high powers
 * on the server.
 */
@RequestScoped
public class CurrentRepositoryProvider {
	@Produces
	@Database(value = DatabaseType.DOCUMENT, provider = "current")
	public DominoDocumentManager getCurrentManager() {
		return new DefaultDominoDocumentCollectionManager(
			() -> NotesContext.getCurrent().getCurrentDatabase(),
			() -> NotesContext.getCurrent().getCurrentSession()
		);
	}
}
