package it.org.openntf.xsp.jakartaee.nsf.transaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import it.org.openntf.xsp.jakartaee.providers.MainAndModuleProvider;
import jakarta.ws.rs.client.Client;

@SuppressWarnings("nls")
public class TestTransactionContext extends AbstractWebClientTest {

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumAndAnonymousClient.class)
	public void testBasicCommit(TestDatabase db, Client client) {
		var target = client.target(getRestUrl(null, db) + "/transaction");
		
		var response = target.request().get();
		checkResponse(200, response);
		
		String content = response.readEntity(String.class);
		assertEquals("committed.", content);
	}

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumAndAnonymousClient.class)
	public void testRollbackOnly(TestDatabase db, Client client) {
		var target = client.target(getRestUrl(null, db) + "/transaction/rollbackOnly");
		
		var response = target.request().get();
		checkResponse(500, response);
		
		String content = response.readEntity(String.class);
		assertTrue(content.startsWith("jakarta.transaction.RollbackException: Transaction was marked as rollback-only and rolled back"), () -> "Unexpected content: " + content);
	}

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumAndAnonymousClient.class)
	public void testAnnotated(TestDatabase db, Client client) {
		var target = client.target(getRestUrl(null, db) + "/transaction/annotated");
		
		var response = target.request().get();
		checkResponse(200, response);
		
		String content = response.readEntity(String.class);
		assertEquals("committed via transactional REST method", content);
	}

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumAndAnonymousClient.class)
	public void testJndi(TestDatabase db, Client client) {
		var target = client.target(getRestUrl(null, db) + "/transaction/jndi");
		
		var response = target.request().get();
		checkResponse(200, response);
		
		String content = response.readEntity(String.class);
		assertTrue(content.startsWith("I found: "), () -> "Unexpected content: " + content);
		assertTrue(content.contains("DominoUserTransaction"), () -> "Unexpected content: " + content);
	}

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumAndAnonymousClient.class)
	public void testJndiManager(TestDatabase db, Client client) {
		var target = client.target(getRestUrl(null, db) + "/transaction/jndi/manager");
		
		var response = target.request().get();
		checkResponse(200, response);
		
		String content = response.readEntity(String.class);
		assertTrue(content.startsWith("I found: "), () -> "Unexpected content: " + content);
		assertTrue(content.contains("DominoTransactionManager"), () -> "Unexpected content: " + content);
	}

	@ParameterizedTest
	@ArgumentsSource(MainAndModuleProvider.EnumAndAnonymousClient.class)
	public void testJndiRegistry(TestDatabase db, Client client) {
		var target = client.target(getRestUrl(null, db) + "/transaction/jndi/registry");
		
		var response = target.request().get();
		checkResponse(200, response);
		
		String content = response.readEntity(String.class);
		assertTrue(content.startsWith("I found: "), () -> "Unexpected content: " + content);
		assertTrue(content.contains("DominoTransactionSynchronizationRegistry"), () -> "Unexpected content: " + content);
	}
}
