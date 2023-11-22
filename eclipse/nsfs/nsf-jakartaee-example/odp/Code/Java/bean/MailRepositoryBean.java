package bean;

import org.openntf.xsp.nosql.communication.driver.DominoDocumentManager;
import org.openntf.xsp.nosql.communication.driver.lsxbe.impl.DefaultDominoDocumentCollectionManager;

import com.ibm.domino.xsp.module.nsf.NotesContext;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.jnosql.mapping.Database;
import org.eclipse.jnosql.mapping.DatabaseType;
import lotus.domino.NotesException;

@RequestScoped
public class MailRepositoryBean {
	@Produces
	@Database(value = DatabaseType.DOCUMENT, provider = "devMail")
	public DominoDocumentManager getNamesManager() {
		return new DefaultDominoDocumentCollectionManager(
			() -> {
				try {
					return NotesContext.getCurrent().getSessionAsSigner().getDatabase("", "dev/jakartamail.nsf");
				} catch (NotesException e) {
					throw new RuntimeException(e);
				}
			},
			() -> NotesContext.getCurrent().getSessionAsSigner()
		);
	}
}
