/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
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
package bean;

import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import model.ExampleDoc;
import model.ExampleDocRepository;
import model.Person;
import model.PersonRepository;

@RequestScoped
public class TransactionBean {
	
	@Inject
	private PersonRepository personRepository;
	
	@Inject
	private ExampleDocRepository repository;
	
	@Transactional
	public Map<String, Object> createExampleDocAndPerson() {
		Person person = new Person();
		person.setFirstName("At " + System.nanoTime());
		person.setLastName("Created for createExampleDocAndPerson");
		person = personRepository.save(person);
		
		ExampleDoc exampleDoc = new ExampleDoc();
		exampleDoc.setTitle("I am created for createExampleDocAndPerson at " + System.nanoTime());
		exampleDoc = repository.save(exampleDoc);
		
		Map<String, Object> result = new HashMap<>();
		result.put("person", person);
		result.put("exampleDoc", exampleDoc);
		return result;
	}
	
	@Transactional
	public Map<String, Object> createExampleDocAndPersonThenFail() {
		Person person = new Person();
		person.setFirstName("At " + System.nanoTime());
		person.setLastName("Created for exampleDocAndPersonTransactionThenFail");
		person = personRepository.save(person);
		
		ExampleDoc exampleDoc = new ExampleDoc();
		exampleDoc.setTitle("I am created for exampleDocAndPersonTransactionThenFail at " + System.nanoTime());
		exampleDoc = repository.save(exampleDoc);
		
		throw new RuntimeException("I am intentionally failing to trigger a rollback");
	}
	
	@Transactional(dontRollbackOn=UnsupportedOperationException.class)
	public Map<String, Object> createExampleDocDontRollback() {
		Person person = new Person();
		person.setFirstName("At " + System.nanoTime());
		person.setLastName("Created for createExampleDocDontRollback");
		person = personRepository.save(person);
		
		Map<String, Object> result = new HashMap<>();
		result.put("person", person);
		throw new UnsupportedOperationException("this is the expected exception to not roll back on");
	}
}
