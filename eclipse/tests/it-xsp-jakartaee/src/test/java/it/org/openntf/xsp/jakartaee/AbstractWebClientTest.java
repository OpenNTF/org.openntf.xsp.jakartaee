package it.org.openntf.xsp.jakartaee;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.ibm.commons.util.PathUtil;

@SuppressWarnings("nls")
@Testcontainers
public abstract class AbstractWebClientTest {

	public Client getAnonymousClient() {
		return ClientBuilder.newBuilder().build();
	}
	public Client getAdminClient() {
		return ClientBuilder.newBuilder().register(AdminUserAuthenticator.class).build();
	}
	
	public String getAppContextPath() {
		return "/dev/jakartaee.nsf";
	}
	
	public String getRootUrl(WebDriver driver) {
		String host;
		int port;
		if(driver instanceof RemoteWebDriver) {
			host = JakartaTestContainers.CONTAINER_NETWORK_NAME;
			port = 80;
		} else {
			host = JakartaTestContainers.instance.domino.getHost();
			port = JakartaTestContainers.instance.domino.getFirstMappedPort();
		}
		
		String context = getAppContextPath();
		return PathUtil.concat("http://" + host + ":" + port, context, '/');
	}
	
	public String getRestUrl(WebDriver driver) {
		String root = getRootUrl(driver);
		return PathUtil.concat(root, "xsp/app", '/');
	}
}
