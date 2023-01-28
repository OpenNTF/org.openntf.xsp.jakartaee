package it.org.openntf.xsp.jakartaee.nsf.jaxrs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.StringReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.SseEventSource;

public class TestSse extends AbstractWebClientTest {
	/**
	 * Tests to see that the SSE endpoint will properly broadcast messages. This
	 * doesn't check for every message being received in order to avoid trouble
	 * with timing.
	 */
	@SuppressWarnings("nls")
	@Test
	public void testChat() throws InterruptedException, ExecutionException {
		Client client = getAnonymousClient();

		WebTarget target = client.target(getRestUrl(null) + "/sseChat");
		// Do an initial request to make sure the app is initialized
		target.request().post(Entity.entity("message=placeholder", MediaType.APPLICATION_FORM_URLENCODED));

		Collection<String> incoming = new HashSet<>();
		Collection<String> outgoing = new HashSet<>();
		
		ScheduledExecutorService exec = Executors.newScheduledThreadPool(2);
		try(SseEventSource source = SseEventSource.target(target).reconnectingEvery(250, TimeUnit.MILLISECONDS).build()) {
			Future<?> reader = exec.schedule(() -> {
				try {
					source.register(evt -> {
						String json = evt.readData();
						JsonObject jsonObj = Json.createReader(new StringReader(json)).readObject();
						incoming.add(jsonObj.getString("message"));
						source.close();
					});
					source.open();
					while(incoming.isEmpty()) {
						TimeUnit.MILLISECONDS.sleep(250);
					}
				} catch(InterruptedException e) {
					// Closing
				} catch(Throwable t) {
					fail(t);
				}
			}, 1, TimeUnit.MILLISECONDS);
			Future<?> writer = exec.schedule(() -> {
				try {
					while(!source.isOpen()) {
						TimeUnit.MILLISECONDS.sleep(100);
					}
					for(int i = 0; i < 5; i++) {
						String msg = "hello" + i;
						outgoing.add(msg);
						Response resp = target.request().post(Entity.entity("message=" + msg, MediaType.APPLICATION_FORM_URLENCODED));
						String content = resp.readEntity(String.class);
						assertEquals(204, resp.getStatus(), () -> "Unexpected result: " + content);
					}
				} catch (InterruptedException e) {
					// Closing
				}
			}, 10, TimeUnit.MILLISECONDS);
			
			writer.get();
			reader.get();
		} finally {
			exec.shutdownNow();
			exec.awaitTermination(10, TimeUnit.SECONDS);
		}

		assertFalse(incoming.isEmpty());
	}
}
