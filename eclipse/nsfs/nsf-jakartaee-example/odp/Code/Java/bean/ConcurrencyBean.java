package bean;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;

@ApplicationScoped
@Named
public class ConcurrencyBean {
	public String getMessage() throws NamingException, InterruptedException, ExecutionException {
		ExecutorService exec = InitialContext.doLookup("java:comp/DefaultManagedExecutorService");
		return exec.submit(() -> "Hello from executor").get();
	}
}
