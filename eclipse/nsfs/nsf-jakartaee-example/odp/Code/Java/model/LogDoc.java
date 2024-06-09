/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package model;

import java.util.List;

import org.openntf.xsp.jakarta.nosql.mapping.extension.DominoRepository;

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
