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
package rest;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openntf.xsp.jakarta.nosql.driver.ExplainEvent;
import org.openntf.xsp.jakarta.nosql.mapping.extension.DominoTemplate;

import bean.NoSQLConfig;
import bean.TransactionBean;
import jakarta.data.Sort;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;
import model.ExampleDoc;
import model.ExampleDocRepository;
import model.Person;
import model.PersonRepository;

@Path("exampleDocs")
public class NoSQLExampleDocs {
	@Inject
	private ExampleDocRepository repository;
	
	@Inject
	private UserTransaction transaction;
	
	@Inject
	private PersonRepository personRepository;
	
	@Inject
	private TransactionBean transactionBean;
	
	@Inject
	private DominoTemplate template;
	
	@Inject
	@Named("dominoSessionAsSigner")
	private Session sessionAsSigner;
	
	@Inject
	private Database database;
	
	@Inject
	private NoSQLConfig nosqlConfig;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<ExampleDoc> get() {
		return repository.findAll().collect(Collectors.toList());
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_JSON)
	public ExampleDoc create(@FormParam("title") String title, @FormParam("categories") List<String> categories, @FormParam("authors") List<String> authors, @FormParam("intentionallyRollBack") boolean intentionallyRollBack) throws Exception {
		transaction.begin();
		try {
			ExampleDoc exampleDoc = new ExampleDoc();
			exampleDoc.setTitle(title);
			exampleDoc.setCategories(categories);
			exampleDoc.setAuthors(authors);
			ExampleDoc result = repository.save(exampleDoc, true);
			if(intentionallyRollBack) {
				throw new RuntimeException("I was asked to intentionally roll back");
			}
			transaction.commit();
			return result;
		} catch(Exception e) {
			transaction.rollback();
			throw e;
		}
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ExampleDoc createJson(ExampleDoc exampleDoc) {
		return repository.save(exampleDoc, true);
	}
	
	@Path("{id}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public ExampleDoc getDoc(@PathParam("id") String id) {
		return repository.findById(id)
			.orElseThrow(() -> new NotFoundException("Could not find example doc for ID " + id));
	}
	
	@Path("{id}")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public ExampleDoc updateDoc(@PathParam("id") String id, ExampleDoc exampleDoc) {
		exampleDoc.setUnid(id);
		return repository.save(exampleDoc, true);
	}
	
	@Path("inView")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<ExampleDoc> getInView(@QueryParam("docsOnly") boolean docsOnly) {
		if(docsOnly) {
			return repository.getViewEntriesDocsOnly().collect(Collectors.toList());
		} else {
			return repository.getViewEntries().collect(Collectors.toList());
		}
	}
	
	@Path("viewCategories")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<ExampleDoc> getViewCategories() {
		return repository.getViewCategories().collect(Collectors.toList());
	}
	
	/**
	 * This method is used for a test to ensure that transactions can be active for two
	 * entities in the same database.
	 */
	@Path("exampleDocAndPersonTransaction")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> createExampleDocAndPerson() throws NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
		transaction.begin();
		try {
			return transactionBean.createExampleDocAndPerson();
		} finally {
			transaction.commit();
		}
	}
	
	/**
	 * This method is used for a test to ensure that transactions can be active for two
	 * entities in the same database and also that the Transactional annotation will prevent
	 * saving to disk
	 */
	@Path("exampleDocAndPersonTransactionThenFail")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> createExampleDocAndPersonThenFail() throws NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
		return transactionBean.createExampleDocAndPersonThenFail();
	}
	
	@Path("exampleDocAndPersonSequential")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> createExampleDocAndPersonSequential() throws NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
		transaction.begin();
		Person person = new Person();
		person.setFirstName("At " + System.nanoTime());
		person.setLastName("Created for createExampleDocAndPersonSequential");
		person = personRepository.save(person);
		transaction.commit();
		
		transaction.begin();
		ExampleDoc exampleDoc = new ExampleDoc();
		exampleDoc.setTitle("I am created for createExampleDocAndPersonSequential at " + System.nanoTime());
		exampleDoc = repository.save(exampleDoc);
		transaction.commit();
		
		Map<String, Object> result = new HashMap<>();
		result.put("person", person);
		result.put("exampleDoc", exampleDoc);
		return result;
	}
	
	/**
	 * This method is used for a test to ensure that transactions can be active for two
	 * entities in the same database.
	 * @throws SystemException 
	 * @throws NotSupportedException 
	 * @throws HeuristicRollbackException 
	 * @throws HeuristicMixedException 
	 * @throws RollbackException 
	 * @throws IllegalStateException 
	 * @throws SecurityException 
	 */
	@Path("exampleDocTransactionDontRollback")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Object> createExampleDocDontRollback() throws NotSupportedException, SystemException, SecurityException, IllegalStateException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
		transaction.begin();
		try {
			transactionBean.createExampleDocDontRollback();
		} catch(Throwable t) {
			t.printStackTrace();
		}
		transaction.commit();
		return Collections.singletonMap("created", true);
	}
	
	/**
	 * Used to test the use of the DQL "contains" operation with a list
	 * 
	 * @param titles the titles to include
	 * @return the matching documents
	 */
	@Path("exampleDocsInTitle")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<ExampleDoc> getTitles(@QueryParam("title") List<String> titles) {
		return template.select(ExampleDoc.class)
			.where("title").in(titles) //$NON-NLS-1$
			.result();
	}
	
	/**
	 * Used to test the use of the DQL "contains" operation with a list
	 * 
	 * @param titles the titles to include
	 * @return the matching documents
	 */
	@Path("exampleDocsInNumberGuy")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<ExampleDoc> getInNumberGuy(@QueryParam("numberGuy") List<Double> numberGuy) {
		return template.select(ExampleDoc.class)
			.where("numberGuy").in(numberGuy) //$NON-NLS-1$
			.result();
	}
	
	/**
	 * Used to test the use of the DQL "contains" operation with a list
	 * 
	 * @param titles the titles to include
	 * @return the matching documents
	 */
	@Path("exampleDocsLikeTitle")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<ExampleDoc> getTitles(@QueryParam("title") String title) {
		return template.select(ExampleDoc.class)
			.where("title").like(title) //$NON-NLS-1$
			.result();
	}
	
	@Path("allSorted")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<ExampleDoc> getAllSorted() {
		return repository.findAllSorted().toList();
	}
	
	@Path("allExplain")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public ExplainEvent getAllExplain() {
		nosqlConfig.setExplainEvents(true);
		try {
			repository.findAll();
			
			ExplainEvent event = nosqlConfig.getLastEvent();
			return event;
		} finally {
			nosqlConfig.setExplainEvents(false);
		}
	}
	
	@Path("allSortedCustom")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<ExampleDoc> getAllSortedCustom() {
		return repository.getViewEntriesCustom(Sort.asc("customSort")).toList();
	}
	
	/**
	 * Utility method to make sure that the FT index is updated for use with
	 * DQL contains
	 * 
	 * @throws NotesException if there is a problem updating the FT index
	 */
	@Path("updateExampleDocFtIndex")
	@POST
	public void updateExampleDocFtIndex() throws NotesException {
		Database databaseAsSigner = sessionAsSigner.getDatabase(database.getServer(), database.getFilePath());
		databaseAsSigner.updateFTIndex(true);
	}
}
