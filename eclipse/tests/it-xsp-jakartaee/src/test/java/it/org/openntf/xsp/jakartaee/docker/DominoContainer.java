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
package it.org.openntf.xsp.jakartaee.docker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.IOUtils;
import org.helmetsrequired.jacocotogo.JaCoCoToGo;
import org.jacoco.agent.AgentJar;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.images.builder.Transferable;

import com.github.dockerjava.api.command.InspectContainerResponse;
import com.ibm.commons.util.PathUtil;
import com.ibm.commons.util.StringUtil;

import it.org.openntf.xsp.jakartaee.TestDatabase;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonPatchBuilder;

public class DominoContainer extends GenericContainer<DominoContainer> {
	private static final String[] BUNDLE_DEPS = {
		"org.openntf.xsp.test.postinstall", //$NON-NLS-1$
		"org.openntf.xsp.test.beanbundle", //$NON-NLS-1$
		"org.openntf.xsp.jakarta.example.webapp", //$NON-NLS-1$
		"org.openntf.xsp.test.jasapi" //$NON-NLS-1$
	};
	public static final int JACOCO_PORT = 6300;
	
	public static final Set<Path> tempFiles = new HashSet<>();
	
	/**
	 * Pattern used to attempt to identify 14.5 EA builds
	 */
	private static final Pattern EA_145_PATTERN = Pattern.compile("^domino-container:V1450_(\\d\\d)(\\d\\d)(\\d\\d\\d\\d)prod$"); //$NON-NLS-1$
	/**
	 * Pattern used to identify 14.5+ official container images
	 */
	private static final Pattern DECIMAL_VERSION_PATTERN = Pattern.compile("^domino-container:(\\d+)\\.(\\d+)(\\.(\\d+))?(FP(\\d+))?$"); //$NON-NLS-1$
	private static final LocalDate DATE_145EA2 = LocalDate.of(2024, 12, 4);

	private static class DominoImage extends ImageFromDockerfile {
		
		public DominoImage() {
			super("xsp-jakartaee-container:1.0.0", true); //$NON-NLS-1$
			withFileFromClasspath("Dockerfile", "/docker/Dockerfile"); //$NON-NLS-1$ //$NON-NLS-2$
			
			String baseImage = System.getProperty("jakarta.baseImage"); //$NON-NLS-1$
			if(StringUtil.isNotEmpty(baseImage)) {
				withBuildArg("BASEIMAGE", baseImage); //$NON-NLS-1$
			}
			
			init();
		}
		
		private void init() {
			// Build temp files to use as volume binds
			try {
				String version = getMavenVersion();

				Path updateSite = findLocalMavenArtifact("org.openntf.xsp", "org.openntf.xsp.jakartaee.updatesite", version, "zip"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				// Copy in the project update site
				try(
					InputStream is = Files.newInputStream(updateSite);
					ZipInputStream zis = new ZipInputStream(is, StandardCharsets.UTF_8)
				) {
					for(ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
						if(!entry.isDirectory()) {
							String bundleName = entry.getName();
							int slashIndex = bundleName.lastIndexOf('/');
							if(slashIndex > -1) {
								bundleName = bundleName.substring(slashIndex+1);
							}
							byte[] data = IOUtils.toByteArray(zis);
							withFileFromTransferable("staging/plugins/" + bundleName, Transferable.of(data)); //$NON-NLS-1$
						}
					}
				}
				
				// Copy in the test-support bundles
				for(String bundleName : BUNDLE_DEPS) {
					Path bundle = findLocalMavenArtifact("org.openntf.xsp", bundleName, version, "jar"); //$NON-NLS-1$ //$NON-NLS-2$
					
					withFileFromPath("staging/plugins/" + bundle.getFileName().toString(), bundle); //$NON-NLS-1$
				}

				
				// Create an Equinox link to the above
				withFileFromClasspath("staging/container.link", "/docker/container.link"); //$NON-NLS-1$ //$NON-NLS-2$
				
				// Next up, copy our Java policy to be the Notes home dir in the container
				withFileFromClasspath("staging/.java.policy", "/docker/java.policy"); //$NON-NLS-1$ //$NON-NLS-2$
				
				// Copy in the Jacoco agent from our dependency tree
				byte[] agentData;
				try(InputStream is = AgentJar.getResourceAsStream()) {
					agentData = IOUtils.toByteArray(is);
				}
				withFileFromTransferable("staging/jacoco.jar", Transferable.of(agentData)); //$NON-NLS-1$
				
				StringBuilder javaOptions = new StringBuilder();
				
				// Configure the JaCoCo listener
				javaOptions.append("-javaagent:/local/jacoco.jar=output=tcpserver,address=*,port=" + JACOCO_PORT); //$NON-NLS-1$
				
				// Loosen the OSGi init timeout to account for having a large bundle footprint
				javaOptions.append("\n-Dosgi.module.lock.timeout=30"); //$NON-NLS-1$
				
				// Add a Java options file for Apple Silicon compatibility
				String arch = DockerClientFactory.instance().getInfo().getArchitecture();
				if(!"x86_64".equals(arch)) { //$NON-NLS-1$
					javaOptions.append("\n-Djava.compiler=NONE"); //$NON-NLS-1$
				}
				
				withFileFromTransferable("staging/JavaOptionsFile.txt", Transferable.of(javaOptions.toString())); //$NON-NLS-1$
				
				// Add the Postgres driver to jvm/lib/ext
				{
					Path postgresJar = findLocalMavenArtifact("org.postgresql", "postgresql", "42.5.4", "jar"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					withFileFromPath("staging/postgresql.jar", postgresJar); //$NON-NLS-1$
				}
				
				// Finally, add our NTFs and Domino config to /local/runner
				{
					JsonObject json;
					try(InputStream is = getClass().getResourceAsStream("/docker/domino-config.json")) { //$NON-NLS-1$
						json = Json.createReader(is).readObject();
					}
					JsonPatchBuilder patch = Json.createPatchBuilder();
					
					// Handle the configured test databases
					for(TestDatabase db : TestDatabase.values()) {
						if(db.isNsf()) {
							Path ntf = findLocalMavenArtifact("org.openntf.xsp", db.getArtifactId(), version, "nsf"); //$NON-NLS-1$ //$NON-NLS-2$
							withFileFromPath("staging/ntf/" + db.getFileName() + ".ntf", ntf); //$NON-NLS-1$ //$NON-NLS-2$
							
							JsonObject dbConfig = Json.createObjectBuilder()
								.add("action", "create") //$NON-NLS-1$ //$NON-NLS-2$
								.add("filePath", "dev/" + db.getFileName() + ".nsf") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								.add("title", db.getTitle()) //$NON-NLS-1$
								.add("templatePath", "/local/runner/" + db.getFileName() + ".ntf") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								.add("signUsingAdminp", true) //$NON-NLS-1$
								.build();
							patch.add("/appConfiguration/databases/-", dbConfig); //$NON-NLS-1$
						}
					}
					
					// Copy in the Jakarta Config NTF
					Path configNtf = findLocalMavenArtifact("org.openntf.xsp", "nsf-jakartaconfig", version, "nsf"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					withFileFromPath("staging/ntf/jakartaconfig.ntf", configNtf); //$NON-NLS-1$
					
					json = patch.build().apply(json);
					withFileFromTransferable("staging/domino-config.json", Transferable.of(json.toString())); //$NON-NLS-1$
				}
				
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		
	}
	
	public DominoContainer() {
		super(new DominoImage());
		
		addEnv("LANG", "en_US.UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
		addEnv("SetupAutoConfigure", "1"); //$NON-NLS-1$ //$NON-NLS-2$
		addEnv("SetupAutoConfigureParams", "/local/runner/domino-config.json"); //$NON-NLS-1$ //$NON-NLS-2$
		addEnv("DOMINO_DOCKER_STDOUT", "yes"); //$NON-NLS-1$ //$NON-NLS-2$
		addEnv("TZ", "Etc/UTC"); //$NON-NLS-1$ //$NON-NLS-2$

		withImagePullPolicy(imageName -> false);
		withExposedPorts(80, JACOCO_PORT);
		withStartupTimeout(Duration.ofMinutes(10));
		
		WaitAllStrategy strat = new WaitAllStrategy()
			.withStrategy(new LogMessageWaitStrategy()
				.withRegEx(".*HTTP Server: Started.*") //$NON-NLS-1$
			)
			.withStrategy(new LogMessageWaitStrategy()
				.withRegEx(".*Done with postinstall.*") //$NON-NLS-1$
			);
		if(useNewEraAdminp()) {
			strat = strat.withStrategy(new LogMessageWaitStrategy()
				.withRegEx(".*Database signed by Adminp.*") //$NON-NLS-1$
				.withTimes(8)
			);
		} else {
			strat = strat.withStrategy(new LogMessageWaitStrategy()
				.withRegEx(".*Adding sign bit to.*") //$NON-NLS-1$
				.withTimes(4000)
			);
		}
		
		waitingFor(strat.withStartupTimeout(Duration.ofMinutes(5)));
	}
	
	private static Path findLocalMavenArtifact(String groupId, String artifactId, String version, String type) {
		String mavenRepo = System.getProperty("maven.repo.local"); //$NON-NLS-1$
		if (StringUtil.isEmpty(mavenRepo)) {
			mavenRepo = PathUtil.concat(System.getProperty("user.home"), ".m2", File.separatorChar); //$NON-NLS-1$ //$NON-NLS-2$
			mavenRepo = PathUtil.concat(mavenRepo, "repository", File.separatorChar); //$NON-NLS-1$
		}
		String groupPath = groupId.replace('.', File.separatorChar);
		Path localPath = Paths.get(mavenRepo).resolve(groupPath).resolve(artifactId).resolve(version);
		String fileName = StringUtil.format("{0}-{1}.{2}", artifactId, version, type); //$NON-NLS-1$
		Path localFile = localPath.resolve(fileName);
		
		if(!Files.isRegularFile(localFile)) {
			throw new RuntimeException("Unable to locate Maven artifact: " + localFile);
		}

		return localFile;
	}
	
	public static String getMavenVersion() {
		// Find the current build version
		Properties props = new Properties();
		try (InputStream is = DominoContainer.class.getResourceAsStream("/scm.properties")) { //$NON-NLS-1$
			props.load(is);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		String version = props.getProperty("git.build.version", null); //$NON-NLS-1$
		if(StringUtil.isEmpty(version)) {
			throw new RuntimeException("Unable to determine artifact version from scm.properties");
		}
		return version;
	}
	
	@SuppressWarnings("nls")
	@Override
	protected void containerIsStopping(InspectContainerResponse containerInfo) {
		try {
			// If we can see the target dir, copy log files
			Path target = Paths.get(".").resolve("target"); //$NON-NLS-1$ //$NON-NLS-2$
			if(Files.isDirectory(target)) {
				this.execInContainer("tar", "-czvf", "/tmp/IBM_TECHNICAL_SUPPORT.tar.gz", "/local/notesdata/IBM_TECHNICAL_SUPPORT");
				this.copyFileFromContainer("/tmp/IBM_TECHNICAL_SUPPORT.tar.gz", target.resolve("IBM_TECHNICAL_SUPPORT.tar.gz").toString());
				
				this.execInContainer("tar", "-czvf", "/tmp/workspace-logs.tar.gz", "/local/notesdata/domino/workspace/logs");
				this.copyFileFromContainer("/tmp/workspace-logs.tar.gz", target.resolve("workspace-logs.tar.gz").toString());
				
				Path output = target.resolve("jacoco.exec");
				int port = this.getMappedPort(JACOCO_PORT);
				JaCoCoToGo.fetchJaCoCoDataOverTcp(this.getHost(), port, output, true);
				
				if(useJfr()) {
					// Dump and grab our JFR output
					// Find the PID for http to pass to jcmd
					String pid = this.execInContainer("pgrep", "http").getStdout();
					if(pid != null) {
						this.execInContainer("/opt/hcl/domino/notes/latest/linux/jvm/bin/jcmd", pid, "JFR.dump");
						this.copyFileFromContainer("/tmp/flight.jfr", target.resolve("flight-" + System.currentTimeMillis() + ".jfr").toString());
					}
				}
				
			}
		} catch(IOException | UnsupportedOperationException | InterruptedException e) {
			e.printStackTrace();
		}
		
		super.containerIsStopping(containerInfo);
	}
	
	/**
	 * Attempts to glean whether the container uses the newer adminp DB-signing
	 * output added in 14.5 EA2
	 */
	public static boolean useNewEraAdminp() {
		String explicitUse = System.getProperty("jakarta.newAdminp"); //$NON-NLS-1$
		if("1".equals(explicitUse)) { //$NON-NLS-1$
			return true;
		}
		
		String baseImage = System.getProperty("jakarta.baseImage"); //$NON-NLS-1$
		Matcher m;
		if(baseImage != null && (m = EA_145_PATTERN.matcher(baseImage)).matches()) {
			LocalDate buildDate = LocalDate.of(Integer.parseInt(m.group(3)), Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
			return !buildDate.isBefore(DATE_145EA2);
		}
		
		if(baseImage != null && (m = DECIMAL_VERSION_PATTERN.matcher(baseImage)).matches()) {
			int major = Integer.parseInt(m.group(1));
			int minor = Integer.parseInt(m.group(2));
			if(major > 14) {
				return true;
			} else if(major == 14 && minor >= 5) {
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean useJfr() {
		String baseImage = System.getProperty("jakarta.baseImage"); //$NON-NLS-1$
		Matcher m;
		if(baseImage != null && (m = EA_145_PATTERN.matcher(baseImage)).matches()) {
			LocalDate buildDate = LocalDate.of(Integer.parseInt(m.group(3)), Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)));
			return !buildDate.isBefore(DATE_145EA2);
		}
		
		if(baseImage != null && (m = DECIMAL_VERSION_PATTERN.matcher(baseImage)).matches()) {
			int major = Integer.parseInt(m.group(1));
			int minor = Integer.parseInt(m.group(2));
			if(major == 14) {
				if(minor == 5) {
					if(m.group(4) != null) {
						// 14.5.1+
						return true;
					} else if(m.group(6) != null) {
						// 14.5FP1+
						return true;
					}
				} else if(minor > 5) {
					// Hypothetical 14.6+
					return true;
				}
			} else if(major > 14) {
				// Hypothetical 15+
				return true;
			}
		}
		
		return false;
	}
}
