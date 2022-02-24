/**
 * Copyright © 2018-2022 Jesse Gallagher
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
	
	public String getExampleContextPath() {
		return "/dev/jakartaee.nsf";
	}
	
	public String getBundleExampleContextPath() {
		return "/dev/jeebundle.nsf";
	}
	
	public String getBaseBundleExampleContextPath() {
		return "/dev/jeebasebundle.nsf";
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
		
		String context = getExampleContextPath();
		return PathUtil.concat("http://" + host + ":" + port, context, '/');
	}
	
	public String getRestUrl(WebDriver driver) {
		String root = getRootUrl(driver);
		return PathUtil.concat(root, "xsp/app", '/');
	}

	public String getServletRestUrl(WebDriver driver) {
		String host;
		int port;
		if(driver instanceof RemoteWebDriver) {
			host = JakartaTestContainers.CONTAINER_NETWORK_NAME;
			port = 80;
		} else {
			host = JakartaTestContainers.instance.domino.getHost();
			port = JakartaTestContainers.instance.domino.getFirstMappedPort();
		}
		
		String context = "/exampleservlet";
		return PathUtil.concat("http://" + host + ":" + port, context, '/');
	}
	
	public String getBudleNsfRootUrl(WebDriver driver) {
		String host;
		int port;
		if(driver instanceof RemoteWebDriver) {
			host = JakartaTestContainers.CONTAINER_NETWORK_NAME;
			port = 80;
		} else {
			host = JakartaTestContainers.instance.domino.getHost();
			port = JakartaTestContainers.instance.domino.getFirstMappedPort();
		}
		
		String context = getBundleExampleContextPath();
		return PathUtil.concat("http://" + host + ":" + port, context, '/');
	}
	
	public String getBaseBudleNsfRootUrl(WebDriver driver) {
		String host;
		int port;
		if(driver instanceof RemoteWebDriver) {
			host = JakartaTestContainers.CONTAINER_NETWORK_NAME;
			port = 80;
		} else {
			host = JakartaTestContainers.instance.domino.getHost();
			port = JakartaTestContainers.instance.domino.getFirstMappedPort();
		}
		
		String context = getBaseBundleExampleContextPath();
		return PathUtil.concat("http://" + host + ":" + port, context, '/');
	}
}
