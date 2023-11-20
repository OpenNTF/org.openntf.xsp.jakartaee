package model;

import org.openntf.xsp.nosql.mapping.extension.DominoRepository;
import org.openntf.xsp.nosql.mapping.extension.RepositoryProvider;

import jakarta.nosql.mapping.Entity;

@Entity("CalendarEntry")
public class CalendarEntry {
	@RepositoryProvider("devMail")
	public interface Repository extends DominoRepository<CalendarEntry, String> {
		
	}
}
