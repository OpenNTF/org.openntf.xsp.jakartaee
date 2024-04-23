package model;

import java.util.List;

import org.openntf.xsp.nosql.mapping.extension.DominoRepository;

import jakarta.nosql.Column;
import jakarta.nosql.Entity;
import jakarta.nosql.Id;

/**
 * Used to test issue #513
 * 
 * @see <a href="https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/513">https://github.com/OpenNTF/org.openntf.xsp.jakartaee/issues/513</a>
 */
@Entity("LogDoc")
public class LogDoc extends DominoDocumentEntityBase {
	public interface Repository extends DominoRepository<LogDoc, String> {
		
	}
	
	@Id
	private String id;

	@Column("log")
	private List<String> log;
	
	@Column("name")
	private String name;
	
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	@Override
	protected List<String> _getLog() { return this.log; }
	@Override
	protected void _setLog(List<String> entries) { this.log = entries; }
}
