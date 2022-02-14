package it.org.openntf.xsp.jakartaee;

import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.firefox.FirefoxOptions;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

import it.org.openntf.xsp.jakartaee.docker.DominoContainer;

public enum JakartaTestContainers {
	instance;
	
	public static final String CONTAINER_NETWORK_NAME = "xsp-jakartaee-test"; //$NON-NLS-1$
	
	public final Network network = Network.builder()
		.driver("bridge") //$NON-NLS-1$
		.build();
	public GenericContainer<?> domino;
	public BrowserWebDriverContainer<?> firefox;
	
	@SuppressWarnings("resource")
	private JakartaTestContainers() {
		try {
			domino = new DominoContainer()
				.withNetwork(network)
				.withNetworkAliases(CONTAINER_NETWORK_NAME)
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
				});
			
			firefox = new BrowserWebDriverContainer<>()
					.withCapabilities(new FirefoxOptions())
					.withNetwork(network);
			
			domino.start();
			// The above waits for "Adding sign bit" from AdminP, but we have no
			//   solid indication when it's done. For now, wait a couple seconds
			try {
				TimeUnit.SECONDS.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			firefox.start();
		} finally {
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				if(domino != null) {
//					domino.close();
				}
				if(firefox != null) {
					firefox.close();
				}
				network.close();
				
				DominoContainer.tempFiles.forEach(t -> {
					try {
						Files.deleteIfExists(t);
					} catch (IOException e) {
						// Ignore
					}
				});
			}));
		}
	}
}
