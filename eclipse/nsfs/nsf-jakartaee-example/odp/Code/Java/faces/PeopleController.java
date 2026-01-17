/**
 * Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
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
package faces;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import lotus.domino.NotesException;
import lotus.domino.Session;
import model.Person;
import model.PersonRepository;

/**
 * Used by person-list.xhtml to mediate access to Person objects
 * 
 * @since 2.13.0
 */
@ViewScoped
@Named
public class PeopleController implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private Person newPerson;
	private Person editingPerson;
	
	@PostConstruct
	public void init() {
		this.newPerson = new Person();
	}
	
	public Person getNewPerson() {
		return newPerson;
	}
	
	public Person getEditingPerson() {
		return editingPerson;
	}
	
	public List<Person> getPeople() {
		PersonRepository repository = CDI.current().select(PersonRepository.class).get();
		return repository.findAll().collect(Collectors.toList());
	}
	
	public void deletePerson(Person person) {
		if(person != null) {
			try{
			PersonRepository repository = CDI.current().select(PersonRepository.class).get();
			repository.deleteById(person.getUnid());
			} catch(Exception e) {
				e.printStackTrace();
				throw e;
			}
		}
	}
	
	public void createPerson(AjaxBehaviorEvent event) {
		if(this.newPerson != null) {
			PersonRepository repository = CDI.current().select(PersonRepository.class).get();
			repository.save(this.newPerson);
			this.newPerson = new Person();
		}
	}
	
	public void editPerson(Person person) {
		this.editingPerson = person;
	}
	
	public void saveEditingPerson(AjaxBehaviorEvent event) {
		if(this.editingPerson != null) {
			PersonRepository repository = CDI.current().select(PersonRepository.class).get();
			repository.save(this.editingPerson);
			this.editingPerson = null;
		}
	}
	
	public boolean isEditable() throws NotesException {
		Session session = CDI.current().select(Session.class, NamedLiteral.of("dominoSession")).get();
		return !"Anonymous".equalsIgnoreCase(session.getEffectiveUserName());
	}
}
