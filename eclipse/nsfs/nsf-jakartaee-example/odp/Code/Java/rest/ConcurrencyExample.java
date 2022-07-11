package rest;

import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;

import javax.naming.InitialContext;

import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.StreamingOutput;
import lotus.domino.Database;
import lotus.domino.Session;

@Path("concurrency")
public class ConcurrencyExample {

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public StreamingOutput get() {
		return os -> {
			PrintWriter w = new PrintWriter(os);
			try {
				ExecutorService exec = InitialContext.doLookup("java:comp/DefaultManagedExecutorService");
				String basic = exec.submit(() -> "Hello from executor").get();
				w.println(basic);
				
				String cdi = exec.submit(() -> "CDI is " + CDI.current()).get();
				w.println(cdi);
				
				String session = exec.submit(() -> "Username is: " + CDI.current().select(Session.class, NamedLiteral.of("dominoSession")).get().getEffectiveUserName()).get();
				w.println(session);
				
				String database = exec.submit(() -> "Database is: " + CDI.current().select(Database.class).get()).get();
				w.println(database);
			} catch(Throwable t) {
				t.printStackTrace(w);
			} finally {
				w.flush();
			}
		};
	}
}
