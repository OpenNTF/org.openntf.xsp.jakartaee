package it.org.openntf.xsp.jakartaee;

import java.io.IOException;
import java.time.Duration;

import org.openqa.selenium.firefox.FirefoxOptions;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public enum JakartaTestContainers {
	instance;
	
	public static final String CONTAINER_NETWORK_NAME = "xsp-jakartaee-test"; //$NON-NLS-1$
	
	public final Network network = Network.builder()
		.driver("bridge") //$NON-NLS-1$
		.build();
	public final GenericContainer<?> domino;
	public final BrowserWebDriverContainer<?> firefox;
	
	@SuppressWarnings("resource")
	private JakartaTestContainers() {
		domino = new GenericContainer<>(DockerImageName.parse("xsp-jakartaee-test:1.0")) //$NON-NLS-1$
			.withExposedPorts(80)
			.withNetwork(network)
			.withNetworkAliases(CONTAINER_NETWORK_NAME)
			.withStartupTimeout(Duration.ofMinutes(4))
			.withLogConsumer(frame -> {
				switch(frame.getType()) {
				case STDERR:
					try {
						System.err.write(frame.getBytes());
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				case STDOUT:
					try {
						System.out.write(frame.getBytes());
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				default:
				case END:
					break;
				}
			})
			.waitingFor(Wait.forHttp("/")); //$NON-NLS-1$
		
		firefox = new BrowserWebDriverContainer<>()
				.withCapabilities(new FirefoxOptions())
				.withNetwork(network);
		
		domino.start();
		firefox.start();
		
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			domino.close();
			firefox.close();
			network.close();
		}));
	}
}
