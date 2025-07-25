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
package it.org.openntf.xsp.jakartaee;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.openqa.selenium.firefox.FirefoxOptions;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;

import it.org.openntf.xsp.jakartaee.docker.DominoContainer;

public enum JakartaTestContainers {
	instance;
	
	public static final String CONTAINER_NETWORK_NAME = "xsp-jakartaee-test"; //$NON-NLS-1$
	
	public final Network network = Network.builder()
		.driver("bridge") //$NON-NLS-1$
		.build();
	public GenericContainer<?> domino;
	public PostgreSQLContainer<?> postgres;
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
			postgres = new PostgreSQLContainer<>("postgres:15.2") //$NON-NLS-1$
				.withUsername("postgres") //$NON-NLS-1$
				.withPassword("postgres") //$NON-NLS-1$
				.withDatabaseName("jakarta") //$NON-NLS-1$
				.withNetwork(network)
				.withNetworkAliases("postgresql") //$NON-NLS-1$
				.withStartupTimeout(Duration.of(2, ChronoUnit.MINUTES));
			postgres.addExposedPort(5432);
			firefox = new BrowserWebDriverContainer<>()
				.withCapabilities(new FirefoxOptions())
				.withNetwork(network);
			
			domino.start();
			postgres.start();
			firefox.start();
			// The above waits for "Adding sign bit" from AdminP, but we have no
			//   solid indication when it's done. For now, wait a couple seconds
			try {
				TimeUnit.SECONDS.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} finally {
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				if(domino != null) {
					domino.close();
				}
				if(postgres != null) {
					postgres.close();
				}
				if(firefox != null) {
					firefox.close();
				}
				network.close();
				
				DominoContainer.tempFiles.forEach(t -> {
					deltree(t);
				});
			}));
		}
	}
	
	private static void deltree(Path path) {
		if(Files.isDirectory(path)) {
			try(Stream<Path> walk = Files.list(path)) {
				walk.forEach(p -> {
					deltree(p);
				});
			} catch(IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		try {
			Files.deleteIfExists(path);
		} catch(IOException e) {
			// This is likely a Windows file-locking thing
			e.printStackTrace();
		}
	}
}
